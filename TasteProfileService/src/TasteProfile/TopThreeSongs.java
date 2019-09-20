package TasteProfile;


/**
* TasteProfile/TopThreeSongs.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from tasteprofile.idl
* Friday, September 20, 2019 6:25:05 PM CEST
*/

public abstract class TopThreeSongs implements org.omg.CORBA.portable.StreamableValue
{
  public TasteProfile.SongCounter topThreeSongs[] = null;

  private static String[] _truncatable_ids = {
    TasteProfile.TopThreeSongsHelper.id ()
  };

  public String[] _truncatable_ids() {
    return _truncatable_ids;
  }

  public void _read (org.omg.CORBA.portable.InputStream istream)
  {
    int _len0 = istream.read_long ();
    this.topThreeSongs = new TasteProfile.SongCounter[_len0];
    for (int _o1 = 0;_o1 < this.topThreeSongs.length; ++_o1)
      this.topThreeSongs[_o1] = TasteProfile.SongCounterHelper.read (istream);
  }

  public void _write (org.omg.CORBA.portable.OutputStream ostream)
  {
    ostream.write_long (this.topThreeSongs.length);
    for (int _i0 = 0;_i0 < this.topThreeSongs.length; ++_i0)
      TasteProfile.SongCounterHelper.write (ostream, this.topThreeSongs[_i0]);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return TasteProfile.TopThreeSongsHelper.type ();
  }
} // class TopThreeSongs
