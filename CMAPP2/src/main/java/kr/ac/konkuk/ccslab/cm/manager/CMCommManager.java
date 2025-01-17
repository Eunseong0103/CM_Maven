package kr.ac.konkuk.ccslab.cm.manager;

import kr.ac.konkuk.ccslab.cm.entity.*;
import kr.ac.konkuk.ccslab.cm.event.CMEventSynchronizer;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.manager.CMInteractionManager;
import kr.ac.konkuk.ccslab.cm.thread.CMByteReceiver;
import kr.ac.konkuk.ccslab.cm.thread.CMByteSender;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CMCommManager {
	
	public static void terminate(CMInfo cmInfo)
	{
		// close all channels in CM
		
		CMCommInfo commInfo = cmInfo.getCommInfo();
		
		//nonblocking serversocket channel (server)
		ServerSocketChannel ssc = commInfo.getNonBlockServerSocketChannel();
		if(ssc != null && ssc.isOpen())
		{
			try {
				ssc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// blocking serversocket channel (server)
		ssc = commInfo.getBlockServerSocketChannel();
		if(ssc != null && ssc.isOpen()){
			try {
				ssc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//nonblocking datagram channel
		CMChannelInfo<Integer> dcInfo = commInfo.getNonBlockDatagramChannelInfo();
		dcInfo.removeAllChannels();
		
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		// socket channels to the default server (client, additional server)
		CMChannelInfo<Integer> scInfo = interInfo.getDefaultServerInfo().getNonBlockSocketChannelInfo();
		scInfo.removeAllChannels();
		scInfo = interInfo.getDefaultServerInfo().getBlockSocketChannelInfo();
		scInfo.removeAllChannels();
		
		// socket channels to additional servers (client)
		Iterator<CMServer> iterServer = interInfo.getAddServerList().iterator();
		while(iterServer.hasNext())
		{
			CMServer tServer = iterServer.next();
			CMChannelInfo<Integer> tscInfo = tServer.getNonBlockSocketChannelInfo();
			tscInfo.removeAllChannels();
			tscInfo = tServer.getBlockSocketChannelInfo();
			tscInfo.removeAllChannels();
		}
		
		// socket channel of users (server)
		Iterator<CMUser> iterUser = interInfo.getLoginUsers().getAllMembers().iterator();
		while(iterUser.hasNext())
		{
			CMUser tUser = iterUser.next();
			CMChannelInfo<Integer> chInfo = tUser.getNonBlockSocketChannelInfo();
			chInfo.removeAllChannels();
			chInfo = tUser.getBlockSocketChannelInfo();
			chInfo.removeAllChannels();
		}
		
		// multicast channel
		Iterator<CMSession> iterSession = interInfo.getSessionList().iterator();
		while(iterSession.hasNext())
		{
			CMSession tSession = iterSession.next();
			Iterator<CMGroup> iterGroup = tSession.getGroupList().iterator();
			while(iterGroup.hasNext())
			{
				CMGroup tGroup = iterGroup.next();
				CMChannelInfo<InetSocketAddress> mcInfo = tGroup.getMulticastChannelInfo();
				mcInfo.removeAllChannels();
			}
		}
		
		if(CMInfo._CM_DEBUG)
			System.out.println("CMCommManager.terminate(), close and remove all channels.");
	}
	
	public static List<String> getLocalIPList()
	{
		String strIPByGetLocalHost = null;
		InetAddress localAddress = null;
		List<InetAddress> myInetAddressList = new ArrayList<InetAddress>();
		List<String> myInetAddressStrList = null;
		
		try {
			localAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		strIPByGetLocalHost = localAddress.getHostAddress();
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("------ detecting IPv4 addresses");
			System.out.println("local address by InetAddress.getLocalHost(): "+strIPByGetLocalHost);
		}
		
		try{

			/// enumerate IP addresses bound to the local host
			Enumeration<NetworkInterface> nienum = NetworkInterface.getNetworkInterfaces();
			//while (nienum.hasMoreElements())
			for(NetworkInterface ni : Collections.list(nienum))
			{
				//NetworkInterface ni = nienum.nextElement();
				
				if(ni.isPointToPoint())
					continue;
				
				if(CMInfo._CM_DEBUG_2)
				{
					System.out.println("network interface name: "+ni.getName());
					System.out.println("  :isLoopback("+ni.isLoopback()+"), isPointToPoint("+ni.isPointToPoint()+
							"), isUp("+ni.isUp()+"), isVirtual("+ni.isVirtual()+")");
				}
				
				Enumeration<InetAddress> enumIA = ni.getInetAddresses();
				//while (enumIA.hasMoreElements())
				for(InetAddress inetAddress : Collections.list(enumIA))
				{
					if( !(inetAddress instanceof Inet4Address) )
						continue;
						
					// add to myInetAddressList
					myInetAddressList.add(inetAddress);
					
					//InetAddress inetAddress = enumIA.nextElement();
					if(CMInfo._CM_DEBUG_2)
					{
						System.out.println("  detected inetAddress: " + inetAddress.getHostAddress());
						System.out.println("    :isLoopback("+inetAddress.isLoopbackAddress()+"), isLinkLocal("
								+inetAddress.isLinkLocalAddress()+"), isSiteLocal("+inetAddress.isSiteLocalAddress()
								+")");
						if(inetAddress instanceof Inet4Address)
							System.out.println("    :detected as the IP4 address");
						else if(inetAddress instanceof Inet6Address)
							System.out.println("    :detected as the IP6 address");
					}
					
					//if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && 
					//inetAddress.isSiteLocalAddress())
//					if(!inetAddress.isLoopbackAddress())
//					{
//						 strIP = inetAddress.getHostAddress().toString();
//						 if(CMInfo._CM_DEBUG_2)
//							 System.out.println("    :detected as the local IP");
//					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		if(myInetAddressList.isEmpty())
		{
			System.err.println("CMCommManager.getLocalIP(), Inet4Address not found!");
			return null;
		}

		// sort the InetAddress list
		// public address > site local address > loopback address
		myInetAddressStrList = myInetAddressList.stream()
				.sorted(
					(addr1, addr2) -> {
						int score1 = 0;
						int score2 = 0;
						score1 += addr1.isLoopbackAddress() ? 3 : 0;
						score1 += addr1.isSiteLocalAddress() ? 2 : 0;
						score1 += addr1.isLinkLocalAddress() ? 1 : 0;
						score2 += addr2.isLoopbackAddress() ? 3 : 0;
						score2 += addr2.isSiteLocalAddress() ? 2 : 0;
						score2 += addr2.isLinkLocalAddress() ? 1 : 0;
						return score1-score2;
					}
				)
				.map(addr -> addr.getHostAddress().toString())
				.collect(Collectors.toList());
		
		if(CMInfo._CM_DEBUG_2) {
			System.out.print("------ detected local IP list: ");
			for(String strAddr : myInetAddressStrList)
				System.out.print(strAddr+" ");
			System.out.println();
		}

		
		return myInetAddressStrList;
	}
	
	public static SelectableChannel openNonBlockChannel(int channelType, String address, int port, CMInfo cmInfo) throws IOException
	{
		SelectableChannel ch = null;
		CMCommInfo commInfo = cmInfo.getCommInfo();
		Selector sel = commInfo.getSelector();
		
		switch(channelType)
		{
		case CMInfo.CM_SERVER_CHANNEL: // address not used
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			ssc.socket().bind(new InetSocketAddress(port));
			ssc.configureBlocking(false);
			ssc.register(sel, SelectionKey.OP_ACCEPT);
			//commInfo.setNonBlockServerSocketChannel(ssc);
			ch = ssc;
			break;
		case CMInfo.CM_SOCKET_CHANNEL:
			SocketChannel sc = SocketChannel.open(new InetSocketAddress(address, port));
			sc.configureBlocking(false);
			sc.register(sel, SelectionKey.OP_READ);
			//commInfo.addSocketChannel(sc);
			ch = sc;
			break;
		case CMInfo.CM_DATAGRAM_CHANNEL:
			DatagramChannel dc = DatagramChannel.open();
			dc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			dc.socket().bind(new InetSocketAddress(address, port));
			dc.configureBlocking(false);
			dc.register(sel, SelectionKey.OP_READ);
			//commInfo.addDatagramChannel(dc);
			ch = dc;
			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			//NetworkInterface ni = NetworkInterface.getByName("eth3");
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
			if(ni == null)
			{
				System.out.println("CMCommManager.openNonBlockChannel(), MULTICAST failed!");
				return null;
			}
			DatagramChannel mc = DatagramChannel.open(StandardProtocolFamily.INET)
					.setOption(StandardSocketOptions.SO_REUSEADDR, true)
					.bind(new InetSocketAddress(port))
					.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
			mc.configureBlocking(false);
			mc.register(sel, SelectionKey.OP_READ);
			ch = mc;
			break;
		default:
			System.out.println("CMCommManager.openNonBlockChannel(), unknown channel type: "+channelType);
			return null;
		}
		
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMCommManager.openNonBlockChannel(), Ok, type("+channelType+"), address("
					+address+"), port("+port+") hashcode("+ch.hashCode()+").");
			System.out.println("# registered selection keys: "+sel.keys().size());
		}
		
		return ch;
	}
	
	public static SelectableChannel openBlockChannel(int channelType, String address, int port, CMInfo cmInfo) throws IOException
	{
		SelectableChannel ch = null;
		
		switch(channelType)
		{
		case CMInfo.CM_SERVER_CHANNEL: // address not used
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			ssc.socket().bind(new InetSocketAddress(port));
			ssc.configureBlocking(true);
			ch = ssc;
			break;
		case CMInfo.CM_SOCKET_CHANNEL:
			SocketChannel sc = SocketChannel.open(new InetSocketAddress(address, port));
			sc.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			sc.configureBlocking(true);
			ch = sc;
			break;
		case CMInfo.CM_DATAGRAM_CHANNEL:
			DatagramChannel dc = DatagramChannel.open();
			dc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			dc.socket().bind(new InetSocketAddress(address, port));
			dc.configureBlocking(true);
			ch = dc;
			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
			if(ni == null)
			{
				System.out.println("CMCommManager.openBlockSocketChannel(), MULTICAST failed!");
				return null;
			}
			DatagramChannel mc = DatagramChannel.open(StandardProtocolFamily.INET)
					.setOption(StandardSocketOptions.SO_REUSEADDR, true)
					.bind(new InetSocketAddress(port))
					.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
			mc.configureBlocking(true);
			ch = mc;
			break;
		default:
			System.out.println("CMCommManager.openBlockSocketChannel(), unknown channel type: "+channelType);
			return null;
		}
		
		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMCommManager.openBlockSocketChannel(), Ok, type("+channelType+"), address("
					+address+"), port("+port+") hashcode("+ch.hashCode()+").");
		}
		
		return ch;		
	}
	
	public static SocketChannel addBlockSocketChannel(int nChKey, String strTarget, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMServer serverInfo = null;
		CMUser targetUser = null;
		SocketChannel sc = null;
		String strTargetSSCAddress = null;
		int nTargetSSCPort = -1;
		CMChannelInfo<Integer> scInfo = null;
		boolean bRet = false;

		//if(getMyself().getState() == CMInfo.CM_INIT || getMyself().getState() == CMInfo.CM_CONNECT)
		if(myself.getState() < CMInfo.CM_LOGIN)
		{
			System.err.println("CMCommManager.addBlockSocketChannel(), you must log in to the default server!");
			return null;
		}
		
		serverInfo = kr.ac.konkuk.ccslab.cm.manager.CMInteractionManager.findServer(strTarget, cmInfo);
		if(serverInfo != null)
		{
			scInfo = serverInfo.getBlockSocketChannelInfo();
			strTargetSSCAddress = serverInfo.getServerAddress();
			nTargetSSCPort = serverInfo.getServerPort();			
		}
		else
		{
			targetUser = CMInteractionManager.findGroupMemberOfClient(strTarget, cmInfo);
			if(targetUser == null)
			{
				System.err.println("CMCommManager.addBlockSocketChannel(), target user("
						+strTarget+") not found!");
				return null;
			}
			
			scInfo = targetUser.getBlockSocketChannelInfo();
			strTargetSSCAddress = targetUser.getHost();
			nTargetSSCPort = targetUser.getSSCPort();
		}
			
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc != null)
		{
			System.err.println("CMCommManager.addBlockSocketChannel(), channel key("
					+nChKey+") to the target("+strTarget+") already exists!");
			return null;
		}
		
		/*
		////////// for Android client where network-related methods must be called in a separate thread
		////////// rather than the MainActivity thread
		CMOpenChannelTask task = new CMOpenChannelTask(CMInfo.CM_SOCKET_CHANNEL,
				strTargetSSCAddress, nTargetSSCPort, true, m_cmInfo);
		ExecutorService es = m_cmInfo.getThreadInfo().getExecutorService();
		Future<SelectableChannel> future = es.submit(task);
		try {
			sc = (SocketChannel) future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		//////////
		*/
		
		try {
			sc = (SocketChannel) openBlockChannel(CMInfo.CM_SOCKET_CHANNEL, strTargetSSCAddress, 
					nTargetSSCPort, cmInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(sc == null)
		{
			System.err.println("CMCommManager.addBlockSocketChannel(), failed!: key("
					+nChKey+"), target("+strTarget+")");
			return null;
		}
		
		scInfo.addChannel(nChKey, sc);
		
		CMSessionEvent se = new CMSessionEvent();
		se.setID(CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL);
		se.setSender(myself.getName());
		se.setReceiver(strTarget);
		se.setChannelName(myself.getName());
		se.setChannelNum(nChKey);
		bRet = CMEventManager.unicastEvent(se, strTarget, CMInfo.CM_STREAM, nChKey, true, cmInfo);
		se = null;

		if(bRet && CMInfo._CM_DEBUG)
		{
			System.out.println("CMCommManager.addBlockSocketChannel(),successfully requested to add the channel "
					+ "with the key("+nChKey+") to the target("+strTarget+")");
		}
				
		return sc;
	}
	
	public static boolean removeBlockSocketChannel(int nChKey, String strTarget, CMInfo cmInfo)
	{
		CMInteractionInfo interInfo = cmInfo.getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMServer serverInfo = null;
		CMUser targetUser = null;
		CMChannelInfo<Integer> scInfo = null;
		boolean result = false;
		SocketChannel sc = null;
		CMSessionEvent se = null;
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();

		if(myself.getState() < CMInfo.CM_LOGIN)
		{
			System.err.println("CMCommManager.removeBlockSocketChannel(), you must log in to the default server!");
			return false;
		}
		
		if(strTarget.equals(strDefServer))
		{
			serverInfo = interInfo.getDefaultServerInfo();
			scInfo = serverInfo.getBlockSocketChannelInfo();
		}
		else if( (serverInfo = interInfo.findAddServer(strTarget)) != null ) 
		{
			scInfo = serverInfo.getBlockSocketChannelInfo();
		}
		else
		{
			String strCurrentSession = myself.getCurrentSession();
			String strCurrentGroup = myself.getCurrentGroup();
			
			CMSession session = interInfo.findSession(strCurrentSession);
			if(session == null)
			{
				System.err.println("CMCommManager.removeBlockSocketChannel(), session("
						+strCurrentSession+") not found!");
				return false;
			}
			CMGroup group = session.findGroup(strCurrentGroup);
			if(group == null)
			{
				System.err.println("CMCommManager.removeBlockSocketChannel(), group("
						+strCurrentGroup+") not found!");
				return false;
			}
			targetUser = group.getGroupUsers().findMember(strTarget);
			if(targetUser == null)
			{
				System.err.println("CMCommManager.removeBlockSocketChannel(), target user("
						+strTarget+") not found!");
				return false;
			}
			
			scInfo = targetUser.getBlockSocketChannelInfo();			
		}
		
		sc = (SocketChannel) scInfo.findChannel(nChKey);
		if(sc == null)
		{
			System.err.println("CMCommManager.removeBlockSocketChannel(), "
					+ "socket channel not found! key("+nChKey+"), target("+strTarget+").");
			return false;
		}
		
		se = new CMSessionEvent();
		se.setID(CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL);
		se.setChannelNum(nChKey);
		se.setChannelName(myself.getName());
		
		//result = send(se, strTarget);	// send the event with the default nonblocking socket channel

		// If targetUser is not null, (that is, if the target is a client instead of a server,)
		// the request event should be forwarded by the default server (internal forwarding of CM).
		se.setSender(myself.getName());
		se.setReceiver(strTarget);		
		if(targetUser != null)
		{
			// set distribution fields
			se.setDistributionSession("CM_ONE_USER");
			se.setDistributionGroup(strTarget);
			
			// send the event to the default server
			result = CMEventManager.unicastEvent(se, strDefServer, cmInfo);
		}
		else
		{
			// send the event to the target
			result = CMEventManager.unicastEvent(se, strTarget, cmInfo);
		}
		se = null;
		
		// The channel will be closed and removed after the client receives the ACK event at the event handler.
		
		return result;
	}
	
	public static MembershipKey joinMulticastGroup(DatagramChannel dc, String addr)
	{
		NetworkInterface ni = null;
		InetAddress group = null;
		MembershipKey key = null;
		
		try {
			//ni = NetworkInterface.getByName("eth3");
			ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			group = InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			key = dc.join(group, ni);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return key;
	}
	
	public static CMByteReceiver startReceivingMessage(CMInfo cmInfo)
	{
		ExecutorService es = cmInfo.getThreadInfo().getExecutorService();
		CMByteReceiver byteReceiver = new CMByteReceiver(cmInfo);
		//byteReceiver.start();
		Future<?> future = es.submit(byteReceiver);
		cmInfo.getCommInfo().setByteReceiver(byteReceiver);
		cmInfo.getCommInfo().setByteReceiverFuture(future);
		
		return byteReceiver;
	}
	
	public static CMByteSender startSendingMessage(CMInfo cmInfo)
	{
		ExecutorService es = cmInfo.getThreadInfo().getExecutorService();
		CMByteSender byteSender = new CMByteSender(cmInfo);
		//byteSender.start();
		Future<?> future = es.submit(byteSender);
		cmInfo.getCommInfo().setByteSender(byteSender);
		cmInfo.getCommInfo().setByteSenderFuture(future);
		
		return byteSender;
	}

	
	public static int sendMessage(ByteBuffer buf, SocketChannel sc)
	{
		int nTotalSentByteNum = 0;
		int nRet = 0;
		
		// initialize the byte buffer
		buf.clear();
		
		while(buf.hasRemaining())
		{
			try {
				nRet = sc.write(buf);
			} catch (IOException e) {
				e.printStackTrace();
				return nTotalSentByteNum;
			}
			
			nTotalSentByteNum += nRet;

			if(CMInfo._CM_DEBUG_2)
			{
				System.out.println("CMCommManager.sendMessage(), SocketChannel has sent "
						+nRet+" bytes.");
			}
		}

		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMCommManager.sendMessage(), SocketChannel completes to send "
					+nTotalSentByteNum+" byets.");
		}
		
		if(buf != null)
			buf = null;
		
		return nTotalSentByteNum;
	}
	
	public static int sendMessage(ByteBuffer buf, DatagramChannel dc, String addr, int port)
	{
		int nTotalSentByteNum = 0;
		int nRet = 0;
		
		// initialize the byte buffer
		buf.clear();
		
		while(buf.hasRemaining())
		{
			try {
				nRet = dc.send(buf, new InetSocketAddress(addr, port));
			} catch (IOException e) {
				e.printStackTrace();
				return nTotalSentByteNum;
			}
			
			nTotalSentByteNum += nRet;

			if(CMInfo._CM_DEBUG_2)
			{
				System.out.println("CMCommManager.sendMessage(), DatagramChannel has sent "
						+nRet+" bytes to ("+addr+", "+port+").");
			}
		}

		if(CMInfo._CM_DEBUG_2)
		{
			System.out.println("CMCommManager.sendMessage(), DatagramChannel completes to send "
					+nTotalSentByteNum+" byets to ("+addr+", "+port+").");
		}
		
		if(buf != null)
			buf = null;
		
		return nTotalSentByteNum;
	}

	public static double measureInputThroughput(String target, CMInfo cmInfo) {
		if(CMInfo._CM_DEBUG) {
			System.out.println("=== CMCommManager.measureInputThroughput() called..");
			System.out.println("target = " + target);
		}

		// check the current thread id
		long threadId = Thread.currentThread().getId();
		CMThreadInfo threadInfo = Objects.requireNonNull(cmInfo.getThreadInfo());
		if(threadId == threadInfo.getEventReceiverId()) {
			System.err.println("The current thread is the event-receiver thread ("+threadId+")!");
			return -1;
		}

		boolean bReturn = false;
		double speed = 0.0;
		long lFileSize = -1;	// the size of a file to measure the transmission delay
		long lTransDelay = -1;
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMEventInfo eInfo = cmInfo.getEventInfo();
		CMEventSynchronizer eventSync = eInfo.getEventSynchronizer();
		CMFileEvent replyEvent = null;
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();

		bReturn = kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager.requestPermitForPullFile(CMInfo.THROUGHPUT_TEST_FILE,
				target, CMInfo.FILE_OVERWRITE, cmInfo);

		if(!bReturn)
			return -1;

		eventSync.init();
		if(confInfo.isFileTransferScheme())
			eventSync.setWaitedEvent(CMInfo.CM_FILE_EVENT, CMFileEvent.END_FILE_TRANSFER_CHAN, target);
		else
			eventSync.setWaitedEvent(CMInfo.CM_FILE_EVENT, CMFileEvent.END_FILE_TRANSFER, target);

		synchronized(eventSync)
		{
			try {
				eventSync.wait(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			replyEvent = (CMFileEvent) eventSync.getReplyEvent();
			if(replyEvent == null)
			{
				System.err.println("CMCommManager.measureInputThroughput(), timeout expired!");
				kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager.cancelPullFile(target, cmInfo);
				return -1;
			}

			lFileSize = replyEvent.getFileSize();
		}

		if(replyEvent.getID() == CMFileEvent.REPLY_PERMIT_PULL_FILE)
		{
			return -1;
		}

		lTransDelay = fInfo.getEndRecvTime() - fInfo.getStartRecvTime();	// millisecond
		speed = ((double)lFileSize / 1000000) / ((double)lTransDelay / 1000);	// MBps

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMCommManager.measureInputThroughput(); received file size("
					+lFileSize+"), delay("+lTransDelay+" ms), speed("+speed+" MBps)");
		}

		return speed;
	}

	public static double measureOutputThroughput(String target, CMInfo cmInfo) {
		if(CMInfo._CM_DEBUG) {
			System.out.println("=== CMCommManager.measureOutputThroughput() called..");
			System.out.println("target = " + target);
		}

		// check the current thread id
		long threadId = Thread.currentThread().getId();
		CMThreadInfo threadInfo = Objects.requireNonNull(cmInfo.getThreadInfo());
		if(threadId == threadInfo.getEventReceiverId()) {
			System.err.println("The current thread is the event-receiver thread ("+threadId+")!");
			return -1;
		}

		boolean bReturn = false;
		double speed = -1;
		long lFileSize = -1;	// the size of a file to measure the transmission delay
		long lTransDelay = -1;
		CMFileTransferInfo fInfo = cmInfo.getFileTransferInfo();
		CMEventInfo eInfo = cmInfo.getEventInfo();
		CMEventSynchronizer eventSync = eInfo.getEventSynchronizer();
		CMFileEvent replyEvent = null;
		CMConfigurationInfo confInfo = cmInfo.getConfigurationInfo();
		String strFilePath = confInfo.getTransferedFileHome().toString() + File.separator + CMInfo.THROUGHPUT_TEST_FILE;

		//bReturn = CMFileTransferManager.pushFile(strFilePath, strTarget, CMInfo.FILE_OVERWRITE, m_cmInfo);
		bReturn = kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager.requestPermitForPushFile(strFilePath, target,
				CMInfo.FILE_OVERWRITE, -1, cmInfo);

		if(!bReturn)
			return -1;

		eventSync.init();
		if(confInfo.isFileTransferScheme())
			eventSync.setWaitedEvent(CMInfo.CM_FILE_EVENT, CMFileEvent.END_FILE_TRANSFER_CHAN_ACK, target);
		else
			eventSync.setWaitedEvent(CMInfo.CM_FILE_EVENT, CMFileEvent.END_FILE_TRANSFER_ACK, target);

		synchronized(eventSync)
		{
			try {
				eventSync.wait(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			replyEvent = (CMFileEvent) eventSync.getReplyEvent();
			if(replyEvent == null)
			{
				System.err.println("CMStub.measureOutputThroughput(), timeout expired!");
				CMFileTransferManager.cancelPushFile(target, cmInfo);
				return -1;
			}

			lFileSize = replyEvent.getFileSize();
		}

		lTransDelay = fInfo.getEndSendTime() - fInfo.getStartSendTime();	// millisecond
		speed = ((double)lFileSize / 1000000) / ((double)lTransDelay / 1000);	// MBps

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMStub.measureOutputThroughput(); received file size("+lFileSize+"), delay("
					+lTransDelay+" ms), speed("+speed+" MBps)");
		}

		return speed;
	}
}
