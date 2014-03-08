package cc.concurrent.mango.support;

import cc.concurrent.mango.ReturnGeneratedId;
import cc.concurrent.mango.SQL;

/**
 * @author ash
 */
public interface ByteInfoDao {

    @ReturnGeneratedId
    @SQL("insert into byte_info(array_byte, single_byte) values(:1, :2)")
    public int insert(byte[] arrayByte, byte singleByte);

    @SQL("select array_byte from byte_info where id=:1")
    public byte[] getArrayByte(int id);

    @SQL("select single_byte from byte_info where single_byte=:1")
    public Byte[] getByteSingles(int singByte);

}
