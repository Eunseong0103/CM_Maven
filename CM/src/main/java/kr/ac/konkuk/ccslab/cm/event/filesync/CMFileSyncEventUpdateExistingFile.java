package kr.ac.konkuk.ccslab.cm.event.filesync;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CMFileSyncEventUpdateExistingFile extends CMFileSyncEvent {
    private int fileEntryIndex;     // client file entry index
    private int numNonMatchBytes;   // number of non-matching bytes
    private byte[] nonMatchBytes;   // array of non-matching bytes
    private int matchBlockIndex;    // matching block index (initial value = -1)

    public CMFileSyncEventUpdateExistingFile() {
        m_nID = CMFileSyncEvent.UPDATE_EXISTING_FILE;
        fileEntryIndex = 0;
        numNonMatchBytes = 0;
        nonMatchBytes = null;
        matchBlockIndex = -1;
    }

    public CMFileSyncEventUpdateExistingFile(ByteBuffer msg) {
        this();
        unmarshall(msg);
    }

    @Override
    protected int getByteNum() {
        int byteNum;
        byteNum = super.getByteNum();
        // fileEntryIndex
        byteNum += Integer.BYTES;
        // numNonMatchBytes
        byteNum += Integer.BYTES;
        // nonMatchBytes
        if(numNonMatchBytes > 0)
            byteNum += nonMatchBytes.length;
        // matchBlockIndex
        byteNum += Integer.BYTES;
        return byteNum;
    }

    @Override
    protected void marshallBody() {
        // fileEntryIndex
        m_bytes.putInt(fileEntryIndex);
        // numNonMatchBytes
        m_bytes.putInt(numNonMatchBytes);
        // nonMatchBytes
        if(numNonMatchBytes > 0)
            m_bytes.put(nonMatchBytes);
        // matchBlockIndex
        m_bytes.putInt(matchBlockIndex);
    }

    @Override
    protected void unmarshallBody(ByteBuffer msg) {
        // fileEntryIndex
        fileEntryIndex = msg.getInt();
        // numNonMatchBytes
        numNonMatchBytes = msg.getInt();
        // nonMatchBytes
        if(numNonMatchBytes > 0) {
            nonMatchBytes = new byte[numNonMatchBytes];
            msg.get(nonMatchBytes);
        }
        // matchBlockIndex
        matchBlockIndex = msg.getInt();
    }

    @Override
    public String toString() {
        return "CMFileSyncEventUpdateExistingFile{" +
                "m_nType=" + m_nType +
                ", m_nID=" + m_nID +
                ", m_strSender='" + m_strSender + '\'' +
                ", m_strReceiver='" + m_strReceiver + '\'' +
                ", m_nByteNum=" + m_nByteNum +
                ", fileEntryIndex=" + fileEntryIndex +
                ", numNonMatchBytes=" + numNonMatchBytes +
                ", nonMatchBytes=" + Arrays.toString(nonMatchBytes) +
                ", matchBlockIndex=" + matchBlockIndex +
                '}';
    }

    public int getFileEntryIndex() {
        return fileEntryIndex;
    }

    public void setFileEntryIndex(int fileEntryIndex) {
        this.fileEntryIndex = fileEntryIndex;
    }

    public int getNumNonMatchBytes() {
        return numNonMatchBytes;
    }

    public void setNumNonMatchBytes(int numNonMatchBytes) {
        this.numNonMatchBytes = numNonMatchBytes;
    }

    public byte[] getNonMatchBytes() {
        return nonMatchBytes;
    }

    public void setNonMatchBytes(byte[] nonMatchBytes) {
        this.nonMatchBytes = nonMatchBytes;
    }

    public int getMatchBlockIndex() {
        return matchBlockIndex;
    }

    public void setMatchBlockIndex(int matchBlockIndex) {
        this.matchBlockIndex = matchBlockIndex;
    }
}
