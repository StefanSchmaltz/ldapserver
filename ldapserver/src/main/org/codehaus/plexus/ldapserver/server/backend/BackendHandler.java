package org.codehaus.plexus.ldapserver.server.backend;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * The BackendHandler is responsible for determining which backend (or backends) need
 * to be called to provide or store information.
 *
 * @author <a href="mailto:clayton.donley@octetstring.com">Clayton Donley</a>
 */

import org.codehaus.plexus.ldapserver.ldapv3.Filter;
import org.codehaus.plexus.ldapserver.ldapv3.LDAPResultEnum;
import org.codehaus.plexus.ldapserver.ldapv3.ModifyRequestSeqOfSeqEnum;
import org.codehaus.plexus.ldapserver.ldapv3.SearchRequestEnum;
import org.codehaus.plexus.ldapserver.server.Credentials;
import org.codehaus.plexus.ldapserver.server.Entry;
import org.codehaus.plexus.ldapserver.server.EntryChange;
import org.codehaus.plexus.ldapserver.server.EntrySet;
import org.codehaus.plexus.ldapserver.server.acl.ACLChecker;
import org.codehaus.plexus.ldapserver.server.schema.SchemaChecker;
import org.codehaus.plexus.ldapserver.server.syntax.DirectoryString;
import org.codehaus.plexus.ldapserver.server.util.DirectoryException;
import org.codehaus.plexus.ldapserver.server.util.DirectorySchemaViolation;
import org.codehaus.plexus.ldapserver.server.util.ServerConfig;

public class BackendHandler
{
  private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(BackendHandler.class);

  private static BackendHandler handler = null;
  private static Hashtable handlerTable = new Hashtable();

  private BackendHandler()
  {
    handlerTable.put(new DirectoryString("cn=schema"), new BackendSchema());
    handlerTable.put(new DirectoryString(""), new BackendRoot());
  }

  public boolean init()
  {
    int numBackends;
    String filename;
    Object backend;

    // Read Backend Properties File
    Properties schemaProp = new Properties();
    try
    {
      filename = (String) ServerConfig.getInstance().get(ServerConfig.JAVALDAP_SERVER_BACKENDS);
      if (filename == null)
      {
        LOGGER.warn("Missing configuration entry '" + ServerConfig.JAVALDAP_SERVER_BACKENDS + "'.");
        return false;
      }

      FileInputStream is = new FileInputStream(filename);
      schemaProp.load(is);
      is.close();
    }
    catch (Exception ex)
    {
      LOGGER.fatal("Exception while loading backend configuration file.", ex);
      return false;
    }

    // Create backends accordingly
    try
    {
      numBackends = new Integer((String) schemaProp.get("backend.num")).intValue();
    }
    catch (NumberFormatException ex)
    {
      LOGGER.fatal("'backend.num' must be a integer.");
      return false;
    }

    if (numBackends < 0)
      LOGGER.warn("No backends configured! The server might not behave as expected.");

    LOGGER.info("Adding " + numBackends + " backends.");
    for (int beCount = 0; beCount < numBackends; beCount++)
    {
      String suffix = (String) schemaProp.get("backend." + beCount + ".root");
      String backendType = (String) schemaProp.get("backend." + beCount + ".type");
      backend = getBackendInstance(backendType);
      if (backend == null)
        return false;
      handlerTable.put(new DirectoryString(suffix), backend);
      LOGGER.info("Backend root: " + suffix + " type: " + backendType);
    }

    return true;
  }

  private Object getBackendInstance(String type)
  {
    try
    {
      return Class.forName(type).newInstance();
    }
    catch (ClassNotFoundException ex)
    {
      LOGGER.warn("Could not find backend class: '" + type + "'", ex);
      return null;
    }
    catch (IllegalAccessException ex)
    {
      LOGGER.warn("Could not access backend class: '" + type + "'", ex);
      return null;
    }
    catch (InstantiationException ex)
    {
      LOGGER.warn("Could not instanciate backend class: '" + type + "'", ex);
      return null;
    }
  }

  public LDAPResultEnum add(Credentials creds, Entry entry) throws DirectorySchemaViolation
  {
    SchemaChecker.getInstance().checkEntry(entry);
    if (!ACLChecker.getInstance().isAllowed(creds, ACLChecker.PERM_ADD, entry.getName()))
    {
      return new LDAPResultEnum(50);
    }

    Backend backend = pickBackend(entry.getName());

    if (backend != null)
    {
      return backend.add(entry);
    }
    else
    {
      return new LDAPResultEnum(32);
    }
  }

  public LDAPResultEnum delete(Credentials creds, DirectoryString name)
  {
    Backend backend = pickBackend(name);
    if (!ACLChecker.getInstance().isAllowed(creds, ACLChecker.PERM_DELETE, name))
    {
      return new LDAPResultEnum(50);
    }
    return backend.delete(name);
  }

  public Vector get(DirectoryString base, int scope, Filter filter,
      boolean typesOnly, Vector attributes) throws DirectoryException
  {

    Vector results = new Vector();
    Vector backends = pickBackends(base, scope);
    Enumeration backEnum = backends.elements();
    while (backEnum.hasMoreElements())
    {
      Backend backend = (Backend) backEnum.nextElement();
      EntrySet partResults = backend.get(base, scope, filter, typesOnly, attributes);
      if (partResults != null && partResults.hasMore())
      {
        results.addElement(partResults);
      }
    }
    return results;
  }

  public Entry getByDN(DirectoryString dn) throws DirectoryException
  {
    Backend backend = pickBackend(dn);
    return backend.getByDN(dn);
  }

  Hashtable getHandlerTable()
  {
    return handlerTable;
  }

  public static BackendHandler Handler()
  {
    if (handler == null)
    {
      handler = new BackendHandler();
    }
    return handler;
  }

  public void modify(Credentials creds, DirectoryString name, Vector changeEntries) throws DirectoryException
  {
    Enumeration changeEnum = changeEntries.elements();
    while (changeEnum.hasMoreElements())
    {
      EntryChange change = (EntryChange) changeEnum.nextElement();
      int changeType = change.getModType();
      DirectoryString attr = change.getAttr();
      if (changeType == ModifyRequestSeqOfSeqEnum.ADD && !ACLChecker.getInstance().isAllowed(creds, ACLChecker.PERM_WRITE, name, attr))
      {
        throw new DirectoryException(50);
      }
      if (changeType == ModifyRequestSeqOfSeqEnum.DELETE && !ACLChecker.getInstance().isAllowed(creds, ACLChecker.PERM_OBLITERATE, name, attr))
      {
        throw new DirectoryException(50);
      }
      if (changeType == ModifyRequestSeqOfSeqEnum.REPLACE && (!ACLChecker.getInstance().isAllowed(creds, ACLChecker.PERM_WRITE, name, attr) ||
          !ACLChecker.getInstance().isAllowed(creds, ACLChecker.PERM_OBLITERATE, name, attr)))
      {
        throw new DirectoryException(50);
      }
    }
    Backend backend = pickBackend(name);
    backend.modify(name, changeEntries);
  }

  public static Backend pickBackend(DirectoryString entryName)
  {
    Backend selected = null;
    int selLength = -1;
    Enumeration backEnum = handlerTable.keys();
    while (backEnum.hasMoreElements())
    {
      DirectoryString base = (DirectoryString) backEnum.nextElement();
      if (entryName.endsWith(base) && base.length() > selLength)
      {
        selected = (Backend) handlerTable.get(base);
        selLength = base.length();
        LOGGER.debug("Switched to " + selected.getClass().getName() + " backend for: " + base);
      }
    }
    return selected;
  }

  public static Vector pickBackends(DirectoryString entryName, int scope)
  {
    Vector backs = new Vector();
    Enumeration backEnum = handlerTable.keys();
    while (backEnum.hasMoreElements())
    {
      DirectoryString base = (DirectoryString) backEnum.nextElement();
      if ((base.length() > 0 && entryName.endsWith(base)) || (scope != SearchRequestEnum.BASEOBJECT && base.endsWith(entryName)))
      {
        backs.addElement(handlerTable.get(base));
        LOGGER.debug("Selected Backend for: " + base);
      }
    }
    return backs;
  }

  public LDAPResultEnum rename(Credentials creds, DirectoryString oldname, DirectoryString newname) throws DirectoryException
  {
    if (!ACLChecker.getInstance().isAllowed(creds, ACLChecker.PERM_RENAMEDN, oldname) ||
        !ACLChecker.getInstance().isAllowed(creds, ACLChecker.PERM_ADD, newname))
    {
      return new LDAPResultEnum(50);
    }
    Backend backend = pickBackend(oldname);
    Backend newBackend = pickBackend(newname);
    if (backend == newBackend)
    {
      return backend.rename(oldname, newname);
    }
    Entry oldEntry = backend.getByDN(oldname);
    oldEntry.setName(newname);
    newBackend.add(oldEntry);
    backend.delete(oldname);
    return new LDAPResultEnum(0);
  }

  public void addBackend(DirectoryString base, Backend backend)
  {
    handlerTable.put(base, backend);
  }
}
