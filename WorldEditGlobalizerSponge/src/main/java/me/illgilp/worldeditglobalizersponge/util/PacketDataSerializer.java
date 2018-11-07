package me.illgilp.worldeditglobalizersponge.util;

import me.illgilp.worldeditglobalizersponge.exceptions.OverflowPacketException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketDataSerializer {

    private ByteArrayOutputStream baos;
    private DataOutputStream bufOut;
    private ByteArrayInputStream bais;
    private DataInputStream bufIn;

    public PacketDataSerializer(byte[] data) {
        baos = new ByteArrayOutputStream();
        try {
            baos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bufOut = new DataOutputStream(baos);
        bais = new ByteArrayInputStream(data);
        bufIn = new DataInputStream(bais);
    }

    public PacketDataSerializer() {
        baos = new ByteArrayOutputStream();
        bufOut = new DataOutputStream(baos);
        bais = new ByteArrayInputStream(new byte[0]);
        bufIn = new DataInputStream(bais);
    }

    public void writeFinalString(String string){
        try {
            bufOut.write(string.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeString(String s)
    {
        if ( s.length() > Short.MAX_VALUE )
        {
            try {
                throw new OverflowPacketException( String.format( "Cannot send string longer than Short.MAX_VALUE (got %s characters)", s.length() ) );
            } catch (OverflowPacketException e) {
                e.printStackTrace();
            }
        }

        byte[] b = s.getBytes(Charset.forName("UTF-8") );
        writeVarInt( b.length );
        try {
            bufOut.write( b );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String readString(int len)
    {
        if ( len > Short.MAX_VALUE )
        {
            try {
                throw new OverflowPacketException( String.format( "Cannot receive string longer than Short.MAX_VALUE (got %s characters)", len ) );
            } catch (OverflowPacketException e) {
                e.printStackTrace();
            }
        }

        byte[] b = new byte[ len ];
        try {
            bufIn.readFully( b );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String( b, Charset.forName("UTF-8") );
    }

    public String readString()
    {
        int len = readVarInt();
        return readString(len);
    }


    public void writeFinalArray(byte[] b)
    {

        try {
            bufOut.write( b );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArray(byte[] b)
    {

        writeVarInt( b.length);
        try {
            bufOut.write( b );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] toArray()
    {
        byte[] ret = new byte[0];
        try {
            ret = new byte[ bufIn.available() ];
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bufIn.read( ret );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public byte[] readArray()
    {
        try {
            return readArray( bufIn.available() );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] readArray(int limit)
    {
        int len = readVarInt();
        if ( len > limit )
        {
            try {
                throw new OverflowPacketException( String.format( "Cannot receive byte array longer than %s (got %s bytes)", limit, len ) );
            } catch (OverflowPacketException e) {
                e.printStackTrace();
            }
        }
        byte[] ret = new byte[ len ];
        try {
            bufIn.read( ret );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void writeStringArray(List<String> s)
    {
        writeVarInt( s.size());
        for ( String str : s )
        {
            writeString( str);
        }
    }

    public List<String> readStringArray()
    {
        int len = readVarInt();
        List<String> ret = new ArrayList<>( len );
        for ( int i = 0; i < len; i++ )
        {
            ret.add( readString() );
        }
        return ret;
    }

    public int readVarInt()
    {
        return readVarInt(5 );
    }

    public int readVarInt( int maxBytes)
    {
        int out = 0;
        int bytes = 0;
        byte in = 0;
        while ( true )
        {
            try {
                in = bufIn.readByte();
            } catch (IOException e) {
                e.printStackTrace();
            }

            out |= ( in & 0x7F ) << ( bytes++ * 7 );

            if ( bytes > maxBytes )
            {
                throw new RuntimeException( "VarInt too big" );
            }

            if ( ( in & 0x80 ) != 0x80 )
            {
                break;
            }
        }

        return out;
    }

    public void writeVarInt(int value)
    {
        int part;
        while ( true )
        {
            part = value & 0x7F;

            value >>>= 7;
            if ( value != 0 )
            {
                part |= 0x80;
            }

            try {
                bufOut.writeByte( part );
            } catch (IOException e) {
                e.printStackTrace();
            }

            if ( value == 0 )
            {
                break;
            }
        }
    }

    public int readVarShort()
    {
        int low = 0;
        try {
            low = bufIn.readUnsignedShort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int high = 0;
        if ( ( low & 0x8000 ) != 0 )
        {
            low = low & 0x7FFF;
            try {
                high = bufIn.readUnsignedByte();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ( ( high & 0xFF ) << 15 ) | low;
    }

    public void writeVarShort(int toWrite)
    {
        int low = toWrite & 0x7FFF;
        int high = ( toWrite & 0x7F8000 ) >> 15;
        if ( high != 0 )
        {
            low = low | 0x8000;
        }
        try {
            bufOut.writeShort( low );
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ( high != 0 )
        {
            try {
                bufOut.writeByte( high );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeUUID(UUID value)
    {
        try {
            bufOut.writeLong( value.getMostSignificantBits() );
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bufOut.writeLong( value.getLeastSignificantBits() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UUID readUUID()
    {
        try {
            return new UUID( bufIn.readLong(), bufIn.readLong() );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void writeByte(byte b){
        try {
            bufOut.writeByte(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte readByte(){
        try {
            return bufIn.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public byte[] toByteArray() {
        return baos.toByteArray();
    }

    public void writeLong(long v) {
        try {
            bufOut.writeLong(v);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long readLong() {
        try {
            return bufIn.readLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void writeInt(int i){
        try {
            bufOut.writeInt(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int readInt(){
        try {
            return bufIn.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void writeBoolean(boolean v) {
        try {
            bufOut.writeBoolean(v);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean readBoolean(){
        try {
            return bufIn.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void writeShort(short in){
        try {
            bufOut.writeShort(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public short readShort(){
        try {
            return bufIn.readShort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public DataOutputStream getBufOut() {
        return bufOut;
    }

    public DataInputStream getBufIn() {
        return bufIn;
    }
}
