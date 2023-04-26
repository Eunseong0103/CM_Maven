import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerEventHandler  implements CMAppEventHandler {
    private CMServerStub m_serverStub;

    public CMServerEventHandler(CMServerStub serverStub) {

        m_serverStub = serverStub;

    }

    @Override
    public void processEvent(CMEvent cme) {
        switch (cme.getType()){
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            default :
                return;
        }
        switch (cme.getType()){
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            default:
                return;
        }
    }

    private void processFileEvent(CMEvent cme) {
        CMFileEvent fe = (CMFileEvent) cme;
        switch (fe.getID()) {
            case CMFileEvent.REPLY_PERMIT_PULL_FILE:
                if (fe.getReturnCode() == -1) {
                    System.err.print("[" + fe.getFileName() + "] does not exist in the owner!\n");
                } else if (fe.getReturnCode() == 0) {
                    System.err.print("[" + fe.getFileSender() + "] rejects to send file("
                            + fe.getFileName() + ").\n");
                }
                break;
        }
    }

    private void processSessionEvent(CMEvent cme){
        CMSessionEvent se = (CMSessionEvent) cme;
        switch (se.getID()){
            case CMSessionEvent.LOGIN:
                System.out.println("["+se.getUserName()+"] requests login.");
                break;
            default:
                return;
        }
    }
}
