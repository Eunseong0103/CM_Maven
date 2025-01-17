package kr.ac.konkuk.ccslab.cm.info;

import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.handler.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMDBInfo;
import kr.ac.konkuk.ccslab.cm.info.CMEventInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileSyncInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMMqttInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.info.CMThreadInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMServiceManager;

import java.util.Hashtable;

public class CMInfo {
	//// global variables

	// minimum number of required threads in CM
	public static final int MIN_NUM_THREADS = 8;

	// general number and lengths setting
	public static final int MAXSESSION_NUM = 64;
	public static final int NAME_NUM = 80;
	public static final int PASSWD_NUM = 256;
	public static final int EVENT_FIELD_LEN = 20;
	public static final int TALK_LEN = 256;
	public static final int DUMMY_LEN = 256;
	public static final int FILE_BLOCK_LEN = 4096;
	public static final int SO_SNDBUF_LEN = 8192;
	public static final int SO_RCVBUF_LEN = 8192;
	public static final int MAX_EVENT_SIZE = 8192;
	public static final int MIN_EVENT_SIZE = 24;
	public static final int STRING_LEN_BYTES_LEN = 2;
	public static final int MQTT_ID_RANGE = 65536;
	public static final int STRONG_CHECKSUM_LEN = 16;	// MD5, 128bit

	// big/little endian
	public static final int CM_BIG_ENDIAN = 0;
	public static final int CM_LITTLE_ENDIAN = 1;
	
	// class types
	public static final int CM_OBJECT = 101;
	public static final int CM_USER = 104;
	public static final int CM_MEMBER = 105;
	public static final int CM_SNS_CONTENT = 106;
	public static final int CM_SNS_CONTENT_LIST = 107;
	
	//public static final int CM_MANAGER = 201;
	public static final int CM_COMM_MANAGER = 202;
	public static final int CM_SESSION_MANAGER = 203;
	public static final int CM_GROUP_MANAGER = 204;
	public static final int CM_EVENT_MANAGER = 205;
	public static final int CM_INTERACTION_MANAGER = 206;
	public static final int CM_FILE_MANAGER = 207;
	public static final int CM_SNS_MANAGER = 208;
	public static final int CM_MQTT_MANAGER = 209;
	public static final int CM_FILE_SYNC_MANAGER = 210;
	
	public static final int CM_EVENT = 301;
	public static final int CM_SESSION_EVENT = 302;
	public static final int CM_DATA_EVENT = 303;
	public static final int CM_CONSISTENCY_EVENT = 304;
	public static final int CM_CONCURRENCY_EVENT = 305;
	public static final int CM_INTEREST_EVENT = 306;
	public static final int CM_DUMMY_EVENT = 312;
	public static final int CM_FILE_EVENT = 313;
	public static final int CM_USER_EVENT = 314;
	public static final int CM_SNS_EVENT = 315;
	public static final int CM_MULTI_SERVER_EVENT = 316;
	public static final int CM_MQTT_EVENT = 317;
	public static final int CM_FILE_SYNC_EVENT = 318;
	
	//public static final int CM_EVENT_HANDLER = 401;
	public static final int CM_MQTT_EVENT_HANDLER = 402;
	public static final int CM_FILE_SYNC_EVENT_HANDLER = 403;
	
	//public static final int CM_COMM_SOCKET = 401;
	//public static final int CM_SERVER_SOCKET = 402;
	//public static final int CM_STREAM_SOCKET = 403;
	//public static final int CM_DATAGRAM_SOCKET = 404;
	//public static final int CM_MULTICAST_SOCKET = 405;
	
	public static final int CM_DATA_MANAGER = 601;
	public static final int CM_CONSISTENCY_MANAGER = 801;
	public static final int CM_CONCURRENCY_MANAGER = 802;
	public static final int CM_INTEREST_MANAGER = 901;
	
	// communication type
	public static final int CM_STREAM = 1;
	public static final int CM_DATAGRAM = 2;
	// communication channel type
	public static final int CM_SERVER_CHANNEL = 1;
	public static final int CM_SOCKET_CHANNEL = 2;
	public static final int CM_DATAGRAM_CHANNEL = 3;
	public static final int CM_MULTICAST_CHANNEL = 4;
	
	// peer states
	public static final int CM_INIT = 1;
	public static final int CM_CONNECT = 2;
	public static final int CM_LOGIN = 3;
	public static final int CM_SESSION_JOIN = 4;
	
	// user event data type
	public static final int CM_INT = 0;
	public static final int CM_LONG = 1;
	public static final int CM_FLOAT = 2;
	public static final int CM_DOUBLE = 3;
	public static final int CM_CHAR = 4;
	public static final int CM_STR = 5;
	public static final int CM_BYTES = 6;
	
	// default session/group manager
	public static final int DEFAULT_SESSION_MANAGER = 1;
	public static final int DEFAULT_GROUP_MANAGER = 1001;
	
	// attachment download schemes
	public static final int SNS_ATTACH_FULL = 0;
	public static final int SNS_ATTACH_PARTIAL = 1;
	public static final int SNS_ATTACH_PREFETCH = 2;
	public static final int SNS_ATTACH_NONE = 3;
	
	// append mode of the file transfer
	public static final byte FILE_DEFAULT = -1;	// determined by the configuration file
	public static final byte FILE_OVERWRITE = 0;
	public static final byte FILE_APPEND = 1;
	
	// level of debug messages
	public static boolean _CM_DEBUG = true;
	public static boolean _CM_DEBUG_2 = false;	// communication level log
	
	// internal file name used for throughput test
	public static final String THROUGHPUT_TEST_FILE = "throughput-test.jpg";

	// temp file prefix for file sync
	public static final String TEMP_FILE_PREFIX = "temp_";
	// directory name to maintain internal files
	public static final String SETTINGS_DIR = ".cm-settings";

	// repository
	private kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo m_confInfo;
	private kr.ac.konkuk.ccslab.cm.info.CMSNSInfo m_snsInfo;
	private kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo m_fileTransferInfo;
	private kr.ac.konkuk.ccslab.cm.info.CMDBInfo m_dbInfo;
	private CMCommInfo m_commInfo;
	private kr.ac.konkuk.ccslab.cm.info.CMEventInfo m_eventInfo;
	private kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo m_interactionInfo;
	private kr.ac.konkuk.ccslab.cm.info.CMThreadInfo m_threadInfo;
	private kr.ac.konkuk.ccslab.cm.info.CMMqttInfo m_mqttInfo;
	private CMFileSyncInfo m_fileSyncInfo;
	
	// CM service manager table
	private Hashtable<Class<? extends CMServiceManager>, Object> serviceManagerHashtable;
	// CM event handler hash table
	private Hashtable<Integer, CMEventHandler> m_eventHandlerHashtable;
	
	// application event handler
	private CMAppEventHandler m_appEventHandler;
	// status info
	private boolean m_bStarted;
	
	public CMInfo()
	{
		m_confInfo = new kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo();
		m_snsInfo = new kr.ac.konkuk.ccslab.cm.info.CMSNSInfo();
		m_fileTransferInfo = new kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo();
		m_dbInfo = new kr.ac.konkuk.ccslab.cm.info.CMDBInfo();
		m_commInfo = new CMCommInfo();
		m_eventInfo = new kr.ac.konkuk.ccslab.cm.info.CMEventInfo();
		m_interactionInfo = new kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo();
		m_threadInfo = new kr.ac.konkuk.ccslab.cm.info.CMThreadInfo();
		m_mqttInfo = new kr.ac.konkuk.ccslab.cm.info.CMMqttInfo();
		m_fileSyncInfo = new CMFileSyncInfo();
		
		serviceManagerHashtable = new Hashtable<>();
		m_eventHandlerHashtable = new Hashtable<Integer, CMEventHandler>();
		
		m_appEventHandler = null;
		m_bStarted = false;
	}

	public synchronized <T extends CMServiceManager> void addServiceManager(Class<T> type, T manager) {
		serviceManagerHashtable.put(type, manager);
	}

	public synchronized <T extends CMServiceManager> T getServiceManager(Class<T> type) {
		T manager = type.cast(serviceManagerHashtable.get(type));
		return manager;
	}
	
	public synchronized CMConfigurationInfo getConfigurationInfo()
	{
		return m_confInfo;
	}
	
	public synchronized CMSNSInfo getSNSInfo()
	{
		return m_snsInfo;
	}
	
	public synchronized CMFileTransferInfo getFileTransferInfo()
	{
		return m_fileTransferInfo;
	}
	
	public synchronized CMDBInfo getDBInfo()
	{
		return m_dbInfo;
	}
	
	public synchronized CMCommInfo getCommInfo()
	{
		return m_commInfo;
	}
	
	public synchronized CMEventInfo getEventInfo()
	{
		return m_eventInfo;
	}
	
	public synchronized CMInteractionInfo getInteractionInfo()
	{
		return m_interactionInfo;
	}
	
	public synchronized CMThreadInfo getThreadInfo()
	{
		return m_threadInfo;
	}
	
	public synchronized CMMqttInfo getMqttInfo()
	{
		return m_mqttInfo;
	}

	public synchronized CMFileSyncInfo getFileSyncInfo() { return m_fileSyncInfo; }
	
	public synchronized Hashtable<Integer, CMEventHandler> getEventHandlerHashtable()
	{
		return m_eventHandlerHashtable;
	}
	
	public synchronized void setAppEventHandler(CMAppEventHandler handler)
	{
		m_appEventHandler = handler;
	}
	
	public synchronized CMAppEventHandler getAppEventHandler()
	{
		return m_appEventHandler;
	}
	
	public synchronized void setStarted(boolean bStarted)
	{
		m_bStarted = bStarted;
	}
	
	public synchronized boolean isStarted()
	{
		return m_bStarted;
	}
}
