import kr.ac.konkuk.ccslab.cm.entity.*;
import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.info.enums.CMFileSyncMode;
import kr.ac.konkuk.ccslab.cm.info.enums.CMTestFileModType;
import kr.ac.konkuk.ccslab.cm.manager.*;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;


public class CMClientApp {
	private CMClientStub m_clientStub;
	private CMClientEventHandler m_eventHandler;
	private boolean m_bRun;
	private Scanner m_scan = null;
	
	public CMClientApp()
	{
		m_clientStub = new CMClientStub();
		m_eventHandler = new CMClientEventHandler(m_clientStub);
		m_bRun = true;
	}
	
	public CMClientStub getClientStub()
	{
		return m_clientStub;
	}
	
	public CMClientEventHandler getClientEventHandler()
	{
		return m_eventHandler;
	}
	
	///////////////////////////////////////////////////////////////
	// test methods

	public void startTest()
	{
		System.out.println("client application starts.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		m_scan = new Scanner(System.in);
		String strInput = null;
		int nCommand = -1;
		while(m_bRun)
		{
			System.out.println("Type \"0\" for menu.");
			System.out.print("> ");
			try {
				strInput = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			try {
				nCommand = Integer.parseInt(strInput);
			} catch (NumberFormatException e) {
				System.out.println("Incorrect command number!");
				continue;
			}

			switch (nCommand) {
				case 0:
					printAllMenus();
					break;
				case 100:
					testStartCM();
					break;
				case 999:
					testTerminateCM();
					break;
				case 1: // connect to default server
					testConnectionDS();
					break;
				case 2: // disconnect from default server
					testDisconnectionDS();
					break;
//				case 3: // connect to a designated server
//					testConnectToServer();
//					break;
//				case 4: // disconnect from a designated server
//					testDisconnectFromServer();
					//break;
				case 10: // asynchronous login to default server
					testLoginDS();
					break;
//				case 11: // synchronously login to default server
//					testSyncLoginDS();
//					break;
				case 12: // logout from default server
					testLogoutDS();
					break;
//				case 13: // log in to a designated server
//					testLoginServer();
//					break;
//				case 14: // log out from a designated server
//					testLogoutServer();
//					break;

				case 70: // set file path
					testSetFilePath();
					break;
				case 71: // request a file
					testRequestFile();
					break;
				case 72: // push a file
					testPushFile();
					break;
				case 73:    // test cancel receiving a file
					cancelRecvFile();
					break;
				case 74:    // test cancel sending a file
					cancelSendFile();
					break;
				case 75:    // print sending/receiving file info
					printSendRecvFileInfo();
					break;
				case 300:    // start file-sync with manual mode
					testStartFileSyncWithManualMode();
					break;
				case 301:    // stop file-sync
					testStopFileSync();
					break;
				case 302:    // open file-sync folder
					testOpenFileSyncFolder();
					break;
				case 303:    // request file-sync online mode
					testRequestFileSyncOnlineMode();
					break;
				case 304:    // request file-sync local mode
					testRequestFileSyncLocalMode();
					break;
				case 305:    // print online mode files
					testPrintOnlineModeFiles();
					break;
				case 306:    // print local mode files
					testPrintLocalModeFiles();
					break;
				case 307:	// start file-sync with auto mode
					testStartFileSyncWithAutoMode();
					break;
				case 308:	// print current file-sync mode
					testPrintCurrentFileSyncMode();
					break;
				default:
					System.err.println("Unknown command.");
					break;
			}
		}
		
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_scan.close();
	}

	public void printAllMenus()
	{
		System.out.println("---------------------------------- Help");
		System.out.println("0: show all menus");
		System.out.println("---------------------------------- Start/Stop");
		System.out.println("100: start CM, 999: terminate CM");
		System.out.println("---------------------------------- Connection");
		System.out.println("1: connect to default server, 2: disconnect from default server");
		//System.out.println("3: connect to designated server, 4: disconnect from designated server");
		System.out.println("---------------------------------- Login");
		System.out.println("10: login to default server");
		System.out.println("12: logout from default server");
		//System.out.println("13: login to designated server, 14: logout from designated server");

		System.out.println("---------------------------------- File Transfer");
		System.out.println("70: set file path, 71: request file, 72: push file");
		System.out.println("73: cancel receiving file, 74: cancel sending file");
		System.out.println("75: print sending/receiving file info");

	}
	
	public void testConnectionDS()
	{
		System.out.println("====== connect to default server");
		m_clientStub.connectToServer();
		System.out.println("======");
	}
	
	public void testDisconnectionDS()
	{
		System.out.println("====== disconnect from default server");
		m_clientStub.disconnectFromServer();
		System.out.println("======");
	}
	
	public void testLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}
		
		System.out.println("====== login to default server");
		System.out.print("user name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strUserName = br.readLine();
			if(console == null)
			{
				System.out.print("password: ");
				strPassword = br.readLine();
			}
			else
				strPassword = new String(console.readPassword("password: "));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
		if(bRequestResult)
			System.out.println("successfully sent the login request.");
		else
			System.err.println("failed the login request!");
		System.out.println("======");
	}
	
	public void testSyncLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		CMSessionEvent loginAckEvent = null;
		Console console = System.console();

		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}
		
		System.out.println("====== login to default server");
		System.out.print("user name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strUserName = br.readLine();
			if(console == null)
			{
				System.out.print("password: ");
				strPassword = br.readLine();
			}
			else
				strPassword = new String(console.readPassword("password: "));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);

		if(loginAckEvent != null)
		{
			// print login result
			if(loginAckEvent.isValidUser() == 0)
			{
				System.err.println("This client fails authentication by the default server!");
			}
			else if(loginAckEvent.isValidUser() == -1)
			{
				System.err.println("This client is already in the login-user list!");
			}
			else
			{
				System.out.println("This client successfully logs in to the default server.");
			}			
		}
		else
		{
			System.err.println("failed the login request!");
		}

		System.out.println("======");		
	}
	
	public void testLogoutDS()
	{
		boolean bRequestResult = false;
		System.out.println("====== logout from default server");
		bRequestResult = m_clientStub.logoutCM();
		if(bRequestResult)
			System.out.println("successfully sent the logout request.");
		else
			System.err.println("failed the logout request!");
		System.out.println("======");
	}
	
	public void testStartCM()
	{
		// get local address
		List<String> localAddressList = CMCommManager.getLocalIPList();
		if(localAddressList == null) {
			System.err.println("Local address not found!");
			return;
		}
		String strCurrentLocalAddress = localAddressList.get(0).toString();
		
		// set config home
		m_clientStub.setConfigurationHome(Paths.get("."));
		// set file-path home
		m_clientStub.setTransferedFileHome(m_clientStub.getConfigurationHome().resolve("client-file-path"));

		// get the saved server info from the server configuration file
		String strSavedServerAddress = null;
		int nSavedServerPort = -1;
		String strNewServerAddress = null;
		String strNewServerPort = null;
		
		strSavedServerAddress = m_clientStub.getServerAddress();
		nSavedServerPort = m_clientStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("========== start CM");
		System.out.println("my current address: "+strCurrentLocalAddress);
		System.out.println("saved server address: "+strSavedServerAddress);
		System.out.println("saved server port: "+nSavedServerPort);
		
		try {
			System.out.print("new server address (enter for saved value): ");
			strNewServerAddress = br.readLine().trim();
			System.out.print("new server port (enter for saved value): ");
			strNewServerPort = br.readLine().trim();

			// update the server info if the user would like to do
			if(!strNewServerAddress.isEmpty() && !strNewServerAddress.equals(strSavedServerAddress))
				m_clientStub.setServerAddress(strNewServerAddress);
			if(!strNewServerPort.isEmpty() && Integer.parseInt(strNewServerPort) != nSavedServerPort)
				m_clientStub.setServerPort(Integer.parseInt(strNewServerPort));
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean bRet = m_clientStub.startCM();
		if(!bRet)
		{
			System.err.println("CM initialization error!");
			return;
		}
		startTest();
	}
	
	public void testTerminateCM()
	{
		m_clientStub.terminateCM();
		m_bRun = false;
	}


	public void testSetFilePath()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== set file path");
		String strPath = null;
		System.out.print("file path: ");
		try {
			strPath = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		m_clientStub.setTransferedFileHome(Paths.get(strPath));
		
		System.out.println("======");
	}
	
	public void testRequestFile()
	{
		boolean bReturn = false;
		String strFileName = null;
		String strFileOwner = null;
		String strFileAppend = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== request a file");
		try {
			System.out.print("File name: ");
			strFileName = br.readLine();
			System.out.print("File owner(enter for \"SERVER\"): ");
			strFileOwner = br.readLine();
//			if(strFileOwner.isEmpty())
//				strFileOwner = m_clientStub.getDefaultServerName();
//			System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
//			strFileAppend = br.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		if(strFileAppend.isEmpty())
//			bReturn = m_clientStub.requestFile(strFileName, strFileOwner);
//		else if(strFileAppend.equals("y"))
//			bReturn = m_clientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_APPEND);
//		else if(strFileAppend.equals("n"))
//			bReturn = m_clientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_OVERWRITE);
//		else
//			System.err.println("wrong input for the file append mode!");
//
//		if(!bReturn)
//			System.err.println("Request file error! file("+strFileName+"), owner("+strFileOwner+").");
//
		m_clientStub.requestFile(strFileName,strFileOwner);
		System.out.println("======");
	}
	
	public void testPushFile()
	{
		String strFilePath = null;
		String strReceiver = null;
		String strFileAppend = null;
		boolean bReturn = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== push a file");
		
		try {
			System.out.print("File path name: ");
			strFilePath = br.readLine();
			System.out.print("File receiver (enter for \"SERVER\"): ");
			strReceiver = br.readLine();
//			if(strReceiver.isEmpty())
//				strReceiver = m_clientStub.getDefaultServerName();
//			System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
//			strFileAppend = br.readLine();

		} catch (IOException e) {
			e.printStackTrace();
		}

//		if(strFileAppend.isEmpty())
//			bReturn = m_clientStub.pushFile(strFilePath, strReceiver);
//		else if(strFileAppend.equals("y"))
//			bReturn = m_clientStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_APPEND);
//		else if(strFileAppend.equals("n"))
//			bReturn = m_clientStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_OVERWRITE);
//		else
//			System.err.println("wrong input for the file append mode!");
//
//		if(!bReturn)
//			System.err.println("Push file error! file("+strFilePath+"), receiver("+strReceiver+")");
//
		m_clientStub.pushFile(strFilePath,strReceiver);
		System.out.println("======");
	}
	
	public void cancelRecvFile()
	{
		String strSender = null;
		boolean bReturn = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== cancel receiving a file");

		System.out.print("Input sender name (enter for all senders): ");
		try {
			strSender = br.readLine();
			if(strSender.isEmpty())
				strSender = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		bReturn = m_clientStub.cancelPullFile(strSender);

		if(bReturn)
		{
			if(strSender == null)
				strSender = "all senders";
			System.out.println("Successfully requested to cancel receiving a file to ["+strSender+"].");
		}
		else
			System.err.println("Request failed to cancel receiving a file to ["+strSender+"]!");

		return;
	}

	public void cancelSendFile()
	{
		String strReceiver = null;
		boolean bReturn = false;
		System.out.println("====== cancel sending a file");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input receiver name (enter for all receivers): ");

		try {
			strReceiver = br.readLine();
			if(strReceiver.isEmpty())
				strReceiver = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		bReturn = m_clientStub.cancelPushFile(strReceiver);

		if(bReturn)
		{
			if(strReceiver == null)
				strReceiver = "all receivers";
			System.out.println("Successfully requested to cancel sending a file to ["+strReceiver+"].");
		}
		else
			System.err.println("Request failed to cancel sending a file to ["+strReceiver+"]!");

		return;
	}
	
	public void printSendRecvFileInfo()
	{
		CMFileTransferInfo fInfo = m_clientStub.getCMInfo().getFileTransferInfo();
		Hashtable<String, CMList<CMSendFileInfo>> sendHashtable = fInfo.getSendFileHashtable();
		Hashtable<String, CMList<CMRecvFileInfo>> recvHashtable = fInfo.getRecvFileHashtable();
		Set<String> sendKeySet = sendHashtable.keySet();
		Set<String> recvKeySet = recvHashtable.keySet();
		
		System.out.print("==== sending file info\n");
		for(String receiver : sendKeySet)
		{
			CMList<CMSendFileInfo> sendList = sendHashtable.get(receiver);
			System.out.print(sendList+"\n");
		}

		System.out.print("==== receiving file info\n");
		for(String sender : recvKeySet)
		{
			CMList<CMRecvFileInfo> recvList = recvHashtable.get(sender);
			System.out.print(recvList+"\n");
		}
	}

	
	public void testConnectToServer()
	{
		System.out.println("====== connect to a designated server");
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input a server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		m_clientStub.connectToServer(strServerName);
		return;
	}
	
	public void testDisconnectFromServer()
	{
		System.out.println("===== disconnect from a designated server");
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input a server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		m_clientStub.disconnectFromServer(strServerName);
		return;
	}
	
	public void testLoginServer()
	{
		String strServerName = null;
		String user = null;
		String password = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("====== log in to a designated server");
		try {
			System.out.print("Input server name: ");
			strServerName = br.readLine();
			if( strServerName.equals(m_clientStub.getDefaultServerName()) )	// login to a default server
			{
				System.out.print("User name: ");
				user = br.readLine();
				if(console == null)
				{
					System.out.print("Password: ");
					password = br.readLine();
				}
				else
				{
					password = new String(console.readPassword("Password: "));
				}
				
				m_clientStub.loginCM(user, password);
			}
			else // use the login info for the default server
			{
				CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
				user = myself.getName();
				password = myself.getPasswd();
				m_clientStub.loginCM(strServerName, user, password);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("======");
		return;
	}
	
	public void testLogoutServer()
	{
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== log out from a designated server");
		System.out.print("Input server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		m_clientStub.logoutCM(strServerName);
		System.out.println("======");
	}
	
	public void testRequestSessionInfoOfServer()
	{
		String strServerName = null;
		System.out.println("====== request session informatino of a designated server");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_clientStub.requestSessionInfo(strServerName);
		System.out.println("======");
		return;
	}


	private void testStartFileSyncWithManualMode() {
		System.out.println("========== start file-sync with manual mode");

		m_eventHandler.setStartTimeOfFileSync(System.currentTimeMillis());

		boolean ret = m_clientStub.startFileSync(CMFileSyncMode.MANUAL);
		if(!ret) {
			System.err.println("Start error of file sync with manual mode!");
			m_eventHandler.setStartTimeOfFileSync(0);
		}
		else {
			System.out.println("File sync with manual mode starts.");
		}
	}

	private void testStartFileSyncWithAutoMode() {
		System.out.println("========== start file-sync with auto mode");

		m_eventHandler.setStartTimeOfFileSync(System.currentTimeMillis());

		boolean ret = m_clientStub.startFileSync(CMFileSyncMode.AUTO);
		if(!ret) {
			System.err.println("Start error of file sync with auto mode!");
			m_eventHandler.setStartTimeOfFileSync(0);
		}
		else {
			System.out.println("File sync with auto mode starts.");
		}
	}

	private void testPrintCurrentFileSyncMode() {
		System.out.println("========== print current file-sync mode");
		CMFileSyncMode currentMode = m_clientStub.getCurrentFileSyncMode();
		if(currentMode == null) {
			System.err.println("Error! Please see error message in console for more information!");
			return;
		}
		System.out.println("Current file-sync mode is "+currentMode+".");
	}

	private void testStopFileSync() {
		System.out.println("========== stop file-sync");
		boolean ret = m_clientStub.stopFileSync();
		if(!ret) {
			System.err.println("Stop error of file sync!");
		}
		else {
			System.out.println("File sync stops.");
		}
	}

	private void testOpenFileSyncFolder() {
		System.out.println("========== open file-sync folder");

		Path syncHome = m_clientStub.getFileSyncHome();
		if(syncHome == null) {
			System.err.println("File sync home is null!");
			System.err.println("Please see error message on console for more information.");
			return;
		}

		// open syncHome folder
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(syncHome.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void testRequestFileSyncOnlineMode() {
		System.out.println("========== request file-sync online mode");
		// get sync home
		CMFileSyncManager syncManager = m_clientStub.findServiceManager(CMFileSyncManager.class);
		Objects.requireNonNull(syncManager);
		Path syncHome = syncManager.getClientSyncHome();

		// open file chooser to choose files
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		fc.setCurrentDirectory(syncHome.toFile());
		int fcRet = fc.showOpenDialog(null);
		if(fcRet != JFileChooser.APPROVE_OPTION) return;
		File[] files = fc.getSelectedFiles();
		if(CMInfo._CM_DEBUG) {
			for(File file : files)
				System.out.println("file = " + file);
		}
		if(files.length < 1) return;

		// call the request API of the client stub
		boolean ret = m_clientStub.requestFileSyncOnlineMode(files);
		if(!ret) {
			System.err.println("request error!");
		}
		return;
	}

	private void testRequestFileSyncLocalMode() {
		System.out.println("========== request file-sync local mode");
		// get sync home
		CMFileSyncManager syncManager = m_clientStub.findServiceManager(CMFileSyncManager.class);
		Objects.requireNonNull(syncManager);
		Path syncHome = syncManager.getClientSyncHome();

		// open file chooser to choose files
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setCurrentDirectory(syncHome.toFile());
		int fcRet = fc.showOpenDialog(null);
		if(fcRet != JFileChooser.APPROVE_OPTION) return;
		File[] files = fc.getSelectedFiles();
		if(CMInfo._CM_DEBUG) {
			for(File file : files)
				System.out.println("file = " + file);
		}
		if(files.length < 1) return;

		// call the request API of the client stub
		boolean ret = m_clientStub.requestFileSyncLocalMode(files);
		if(!ret) {
			System.out.println("request error!");
		}
		return;
	}

	private void testPrintOnlineModeFiles() {
		System.out.println("========== print online mode files");
		List<Path> onlineModeFiles = m_clientStub.getOnlineModeFiles();
		if(onlineModeFiles == null) {
			System.err.println("online mode file list is null!");
			System.err.println("Please check error message in console for more information!");
			return;
		}

		for(Path path : onlineModeFiles) {
			System.out.println(path);
		}
	}

	private void testPrintLocalModeFiles() {
		System.out.println("========== print local mode files");
		List<Path> localModeFiles = m_clientStub.getLocalModeFiles();
		if(localModeFiles == null) {
			System.err.println("local mode file list is null!");
			System.err.println("Please check error message in console for more information!");
			return;
		}

		for(Path path : localModeFiles) {
			System.out.println(path);
		}
	}




	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		CMClientApp client = new CMClientApp();
		CMClientStub cmStub = client.getClientStub();

		cmStub.setAppEventHandler(client.getClientEventHandler());
		client.testStartCM();
		System.out.println("Client application is terminated.");

		CMClientEventHandler eventHandler = client.getClientEventHandler();
		boolean ret = false;

		//initialize CM
		cmStub.setAppEventHandler(eventHandler);
		ret = cmStub.startCM();

		if(ret)
			System.out.println("init success");
		else {
			System.err.println("init error!");
			return;
		}

		//login CM server
		System.out.println("user name: ccslab");
		System.out.println("password: cclsab");
		ret = cmStub.loginCM("ccslab","ccslab");
		if(ret){
			System.out.println("Successfully sent the login request");
		}
		else{
			System.err.println("failed the login request");
		}


	}

}
