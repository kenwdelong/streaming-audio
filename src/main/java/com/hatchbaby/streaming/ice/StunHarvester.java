package com.hatchbaby.streaming.ice;

import org.ice4j.TransportAddress;
import org.ice4j.ice.HostCandidate;
import org.ice4j.ice.harvest.StunCandidateHarvest;
import org.ice4j.ice.harvest.StunCandidateHarvester;

public class StunHarvester extends StunCandidateHarvester
{

	public StunHarvester(TransportAddress stunServer)
	{
		super(stunServer);
	}

	@Override
    protected StunCandidateHarvest createHarvest(HostCandidate hostCandidate)
    {
        //return new StunCandidateHarvest(this, hostCandidate);
		return new StunHarvest(this, hostCandidate, 50);
    }

	
//	@Override
//	protected LongTermCredential createLongTermCredential(StunCandidateHarvest harvest, byte[] realm)
//    {
//        //return null;
//		
//		String username = "";
//		String password = "";
//        return new LongTermCredential(username, password);
//    }
	
	public static class StunHarvest extends StunCandidateHarvest
	{

		public StunHarvest(StunCandidateHarvester harvester, HostCandidate hostCandidate, long keepAliveInterval)
		{
			super(harvester, hostCandidate);
			setSendKeepAliveMessageInterval(keepAliveInterval);
		}
		
	}
}
