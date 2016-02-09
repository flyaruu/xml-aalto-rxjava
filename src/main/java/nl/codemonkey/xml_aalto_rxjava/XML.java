package nl.codemonkey.xml_aalto_rxjava;

import rx.Observable.Operator;
import rx.Subscriber;

public class XML {
	public static Operator<XMLEvent,byte[]> parse() {
		return new Operator<XMLEvent,byte[]>(){
			private final SaxXmlFeeder feeder = new SaxXmlFeeder();
			
			@Override
			public Subscriber<? super byte[]> call(Subscriber<? super XMLEvent> in) {
				return new Subscriber<byte[]>() {

					@Override
					public void onCompleted() {
						feeder.endOfInput(in);
						
					}

					@Override
					public void onError(Throwable e) {
						if(!in.isUnsubscribed()) {
							in.onError(e);
						}
					}

					@Override
					public void onNext(byte[] bytes) {
						if(!in.isUnsubscribed()) {
							feeder.parse(bytes,in);
						}
					}
				};
			}};
		
	}

}
