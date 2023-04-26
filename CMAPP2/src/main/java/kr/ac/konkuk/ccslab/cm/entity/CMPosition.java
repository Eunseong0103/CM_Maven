package kr.ac.konkuk.ccslab.cm.entity;

import kr.ac.konkuk.ccslab.cm.entity.CMPoint3f;
import kr.ac.konkuk.ccslab.cm.entity.CMQuat;

/**
 * This class represents location information.
 * <br> The location information consists of the position and orientation.
 * @author mlim
 *
 */
public class CMPosition {
	public CMPoint3f m_p;
	public kr.ac.konkuk.ccslab.cm.entity.CMQuat m_q;
	
	public CMPosition()
	{
		m_p = new CMPoint3f();
		m_q = new CMQuat();
	}
}
