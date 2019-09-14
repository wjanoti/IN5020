package TasteProfile;


/**
* TasteProfile/SongCounter.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from tasteprofile.idl
* Saturday, September 14, 2019 4:20:22 PM CEST
*/

public abstract class SongCounter implements org.omg.CORBA.portable.StreamableValue
{
  public String song_id = null;
  public int songid_play_time = (int)0;

  private static String[] _truncatable_ids = {
    TasteProfile.SongCounterHelper.id ()
  };

  public String[] _truncatable_ids() {
    return _truncatable_ids;
  }

  public void _read (org.omg.CORBA.portable.InputStream istream)
  {
    this.song_id = istream.read_string ();
    this.songid_play_time = istream.read_long ();
  }

  public void _write (org.omg.CORBA.portable.OutputStream ostream)
  {
    ostream.write_string (this.song_id);
    ostream.write_long (this.songid_play_time);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return TasteProfile.SongCounterHelper.type ();
  }
} // class SongCounter
