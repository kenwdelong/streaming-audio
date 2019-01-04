package com.hatchbaby.streaming.ice;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.ice4j.ice.Agent;
import org.ice4j.ice.CandidatePair;
import org.ice4j.ice.Component;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.IceProcessingState;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class StateListener implements PropertyChangeListener
{
	private final BehaviorSubject<CandidatePair> subject = BehaviorSubject.create();

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if(evt.getSource() instanceof Agent)
		{
			Agent agent = (Agent) evt.getSource();
			if(agent.getState().equals(IceProcessingState.TERMINATED))
			{
				for(IceMediaStream stream : agent.getStreams())
				{
					if(stream.getName().contains("audio"))
					{
						Component rtpComponent = stream.getComponent(org.ice4j.ice.Component.RTP);
						CandidatePair rtpPair = rtpComponent.getSelectedPair();
						subject.onNext(rtpPair);
					}
				}
			}
		}
	}

	public Observable<CandidatePair> toObservable()
	{
		return subject.asObservable();
	}
}
