package org.codehaus.plexus.ldapserver.ldapv3;

import java.io.PrintStream;

//-----------------------------------------------------------------------------
//   NOTE: this is a machine generated file - editing not recommended
//
//   File: ./src/org/codehaus/ldapv3/BindResponse.java
//
//   Java class for ASN.1 definition BindResponse as defined in
//   module LDAPv3.
//   This file was generated by Snacc for Java at Fri Jul  2 18:01:43 1999
//   Snacc for Java - Andreas Schade (SAN/ZRL)
//-----------------------------------------------------------------------------

// Import PrintStream class for print methods
import com.ibm.asn1.ASN1Decoder;
import com.ibm.asn1.ASN1Encoder;
import com.ibm.asn1.ASN1Exception;
import com.ibm.asn1.ASN1Tag;
import com.ibm.util.HexOutputStream;

/** This class represents the ASN.1 SEQUENCE type <tt>BindResponse</tt>.
 * For each sequence member, sequence classes contain a
 * public member variable of the corresponding Java type.
 * @author Snacc for Java
 * @version Fri Jul  2 18:01:43 1999

 */

public class BindResponse implements LDAPv3
{

  /** member variable representing the sequence member resultCode of type LDAPResultEnum */
  public LDAPResultEnum resultCode = new LDAPResultEnum();
  /** member variable representing the sequence member matchedDN of type byte[] */
  public byte[] matchedDN;
  /** member variable representing the sequence member errorMessage of type byte[] */
  public byte[] errorMessage;
  /** member variable representing the sequence member referral of type Referral */
  public Referral referral = null;
  /** member variable representing the sequence member serverSaslCreds of type byte[] */
  public byte[] serverSaslCreds = null;

  /** default constructor */
  public BindResponse()
  {
  }

  /** copy constructor */
  public BindResponse( BindResponse arg )
  {
    resultCode = new LDAPResultEnum( arg.resultCode );
    matchedDN = new byte[arg.matchedDN.length];
    System.arraycopy( arg.matchedDN, 0, matchedDN, 0, arg.matchedDN.length );
    errorMessage = new byte[arg.errorMessage.length];
    System.arraycopy( arg.errorMessage, 0, errorMessage, 0, arg.errorMessage.length );
    referral = new Referral( arg.referral );
    serverSaslCreds = new byte[arg.serverSaslCreds.length];
    System.arraycopy( arg.serverSaslCreds, 0, serverSaslCreds, 0, arg.serverSaslCreds.length );
  }

  /** decoding method.
   * @param dec
   *        decoder object derived from com.ibm.asn1.ASN1Decoder
   * @exception com.ibm.asn1.ASN1Exception
   *            decoding error
   */
  @Override
  public void decode( ASN1Decoder dec ) throws ASN1Exception
  {
    dec.nextIsImplicit( ASN1Tag.makeTag( ASN1Tag.APPLICATION_TAG_CLASS, 1 ) );
    int seq_nr = dec.decodeSequence();
    resultCode.decode( dec );
    matchedDN = dec.decodeOctetString();
    errorMessage = dec.decodeOctetString();
    if ( !dec.nextIsOptional( ASN1Tag.makeTag( ASN1Tag.CONTEXT_TAG_CLASS, 3 ) ) )
    {
      referral = new Referral();
      dec.nextIsImplicit( ASN1Tag.makeTag( ASN1Tag.CONTEXT_TAG_CLASS, 3 ) );
      referral.decode( dec );
    }
    if ( !dec.nextIsOptional( ASN1Tag.makeTag( ASN1Tag.CONTEXT_TAG_CLASS, 7 ) ) )
    {
      dec.nextIsImplicit( ASN1Tag.makeTag( ASN1Tag.CONTEXT_TAG_CLASS, 7 ) );
      serverSaslCreds = dec.decodeOctetString();
    }
    dec.endOf( seq_nr );
  }

  /** encoding method.
   * @param enc
   *        encoder object derived from com.ibm.asn1.ASN1Encoder
   * @exception com.ibm.asn1.ASN1Exception
   *            encoding error
   */
  @Override
  public void encode( ASN1Encoder enc ) throws ASN1Exception
  {
    enc.nextIsImplicit( ASN1Tag.makeTag( ASN1Tag.APPLICATION_TAG_CLASS, 1 ) );
    int seq_nr = enc.encodeSequence();
    resultCode.encode( enc );
    enc.encodeOctetString( matchedDN );
    enc.encodeOctetString( errorMessage );
    if ( referral != null )
    {
      enc.nextIsImplicit( ASN1Tag.makeTag( ASN1Tag.CONTEXT_TAG_CLASS, 3 ) );
      referral.encode( enc );
    }
    if (serverSaslCreds != null)
    {
      enc.nextIsImplicit( ASN1Tag.makeTag( ASN1Tag.CONTEXT_TAG_CLASS, 7 ) );
      enc.encodeOctetString( serverSaslCreds );
    }
    enc.endOf( seq_nr );
  }

  /** default print method (variable indentation)
   * @param os
   *        PrintStream representing the print destination (file, etc)
   */
  public void print( PrintStream os )
  {
    print( os, 0 );
  }

  /** print method (variable indentation)
   * @param os
   *        PrintStream representing the print destination (file, etc)
   * @param indent
   *        number of blanks that preceed each output line.
   */
  public void print( PrintStream os, int indent )
  {
    os.println( "{ -- SEQUENCE --" );
    for ( int ii = 0; ii < indent + 2; ii++ ) os.print( ' ' );
    os.print( "resultCode = " );
    resultCode.print( os, indent + 2 );
    os.println( ',' );

    for ( int ii = 0; ii < indent + 2; ii++ ) os.print( ' ' );
    os.print( "matchedDN = " );
    try
    {
      ( new HexOutputStream( os ) ).write( matchedDN );
    }
    catch ( java.io.IOException ex )
    {
      os.print( "( unprintable OCTET STRING value )" );
    }
    os.println( ',' );

    for ( int ii = 0; ii < indent + 2; ii++ ) os.print( ' ' );
    os.print( "errorMessage = " );
    try
    {
      ( new HexOutputStream( os ) ).write( errorMessage );
    }
    catch ( java.io.IOException ex )
    {
      os.print( "( unprintable OCTET STRING value )" );
    }

    os.println( ',' );
    if ( referral != null )
    {
      for ( int ii = 0; ii < indent + 2; ii++ ) os.print( ' ' );
      os.print( "referral = " );
      referral.print( os, indent + 2 );
    }

    os.println( ',' );
    if ( serverSaslCreds != null )
    {
      for ( int ii = 0; ii < indent + 2; ii++ ) os.print( ' ' );
      os.print( "serverSaslCreds = " );
      try
      {
        ( new HexOutputStream( os ) ).write( serverSaslCreds );
      }
      catch ( java.io.IOException ex )
      {
        os.print( "( unprintable OCTET STRING value )" );
      }
    }

    os.println();
    for ( int ii = 0; ii < indent; ii++ ) os.print( ' ' );
    os.print( '}' );
  }
}
