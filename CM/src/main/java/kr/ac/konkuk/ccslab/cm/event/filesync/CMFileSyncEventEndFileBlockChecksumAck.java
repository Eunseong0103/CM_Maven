package kr.ac.konkuk.ccslab.cm.event.filesync;

import java.nio.ByteBuffer;

public class CMFileSyncEventEndFileBlockChecksumAck extends CMFileSyncEvent {
    private int fileEntryIndex; // client file entry index
    private int totalNumBlocks; // total number of blocks of this file
    private int blockSize;      // block size
    private int fileChecksum;   // file checksum (sum of block weakchecksum modular M)
    private int returnCode;

    public CMFileSyncEventEndFileBlockChecksumAck() {
        m_nID = CMFileSyncEvent.END_FILE_BLOCK_CHECKSUM_ACK;
        fileEntryIndex = 0;
        totalNumBlocks = 0;
        blockSize = 0;
        fileChecksum = 0;
        returnCode = -1;
    }

    public CMFileSyncEventEndFileBlockChecksumAck(ByteBuffer msg) {
        this();
        unmarshall(msg);
    }

    @Override
    protected int getByteNum() {
        int byteNum;
        byteNum = super.getByteNum();
        // fileEntryIndex
        byteNum += Integer.BYTES;
        // totalNumBlocks
        byteNum += Integer.BYTES;
        // blockSize
        byteNum += Integer.BYTES;
        // fileChecksum
        byteNum += Integer.BYTES;
        // returnCode
        byteNum += Integer.BYTES;
        return byteNum;
    }

    @Override
    protected void marshallBody() {
        // fileEntryIndex
        m_bytes.putInt(fileEntryIndex);
        // totalNumBlocks
        m_bytes.putInt(totalNumBlocks);
        // blocksSize
        m_bytes.putInt(blockSize);
        // fileChecksum
        m_bytes.putInt(fileChecksum);
        // returnCode
        m_bytes.putInt(returnCode);
    }

    @Override
    protected void unmarshallBody(ByteBuffer msg) {
        // fileEntryIndex
        fileEntryIndex = msg.getInt();
        // totalNumBlocks
        totalNumBlocks = msg.getInt();
        // blocksSize
        blockSize = msg.getInt();
        // fileChecksum
        fileChecksum = msg.getInt();
        // returnCode
        returnCode = msg.getInt();
    }

    @Override
    public String toString() {
        return "CMFileSyncEventEndFileBlockChecksumAck{" +
                "m_nType=" + m_nType +
                ", m_nID=" + m_nID +
                ", m_strSender='" + m_strSender + '\'' +
                ", m_strReceiver='" + m_strReceiver + '\'' +
                ", m_nByteNum=" + m_nByteNum +
                ", fileEntryIndex=" + fileEntryIndex +
                ", totalNumBlocks=" + totalNumBlocks +
                ", blockSize=" + blockSize +
                ", fileChecksum=" + fileChecksum +
                ", returnCode=" + returnCode +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)) return false;
        if(!(obj instanceof CMFileSyncEventEndFileBlockChecksumAck fse)) return false;
        return fse.getBlockSize() == blockSize &&
                fse.getFileChecksum() == fileChecksum &&
                fse.getFileEntryIndex() == fileEntryIndex &&
                fse.getTotalNumBlocks() == totalNumBlocks &&
                fse.getReturnCode() == returnCode;
    }

    public int getFileEntryIndex() {
        return fileEntryIndex;
    }

    public void setFileEntryIndex(int fileEntryIndex) {
        this.fileEntryIndex = fileEntryIndex;
    }

    public int getTotalNumBlocks() {
        return totalNumBlocks;
    }

    public void setTotalNumBlocks(int totalNumBlocks) {
        this.totalNumBlocks = totalNumBlocks;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(int fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
}