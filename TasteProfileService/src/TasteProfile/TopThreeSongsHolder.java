package TasteProfile;

/**
* TasteProfile/TopThreeSongsHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from tasteprofile.idl
* Tuesday, September 24, 2019 8:02:27 PM CEST
*/

public final class TopThreeSongsHolder implements org.omg.CORBA.portable.Streamable
{
  public TasteProfile.TopThreeSongs value = null;

  public TopThreeSongsHolder ()
  {
  }

  public TopThreeSongsHolder (TasteProfile.TopThreeSongs initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = TasteProfile.TopThreeSongsHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    TasteProfile.TopThreeSongsHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return TasteProfile.TopThreeSongsHelper.type ();
  }

}
