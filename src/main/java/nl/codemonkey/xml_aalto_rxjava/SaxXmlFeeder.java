package nl.codemonkey.xml_aalto_rxjava;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import nl.codemonkey.xml_aalto_rxjava.XMLEvent.XmlEventTypes;
import rx.Subscriber;

public class SaxXmlFeeder {

	private final AsyncByteArrayFeeder wrappedFeeder;
	private final AsyncXMLStreamReader<AsyncByteArrayFeeder> parser;

	// private Throwable failedWith = null;

	public SaxXmlFeeder() {
		AsyncXMLInputFactory f = new InputFactoryImpl();
		this.parser = f.createAsyncForByteArray();
		this.wrappedFeeder = parser.getInputFeeder();

	}

	public void endOfInput(Subscriber<? super XMLEvent> in) {
		wrappedFeeder.endOfInput();
		parse(new byte[] {}, in);
		in.onCompleted();
	}

	// public Throwable getException() {
	// return this.failedWith;
	// }

	public void parse(byte[] buffer, Subscriber<? super XMLEvent> subscriber) {
		try {
			if (buffer.length > 0) {
				wrappedFeeder.feedInput(buffer, 0, buffer.length);
			}
			int currentToken = -1;
			while (parser.hasNext() && currentToken != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
				currentToken = parser.next();
				if(currentToken!=AsyncXMLStreamReader.EVENT_INCOMPLETE) {
					subscriber.onNext(processEvent(currentToken));
				}
			}
		} catch (XMLStreamException e) {
			subscriber.onError(e);
		}
	}

	private XMLEvent processEvent(int token) {

		switch (token) {
		case XMLStreamConstants.START_DOCUMENT:
			return new XMLEvent(XmlEventTypes.START_DOCUMENT, null, null);
		case XMLStreamConstants.END_DOCUMENT:
			return new XMLEvent(XmlEventTypes.END_DOCUMENT, null, null);
		case XMLStreamConstants.START_ELEMENT:
			Map<String, String> attributes = new HashMap<>();
			for (int i = 0; i < parser.getAttributeCount(); i++) {
				attributes.put(parser.getAttributeLocalName(i), parser.getAttributeValue(i));
			}
			return new XMLEvent(XmlEventTypes.START_ELEMENT, parser.getLocalName(),
					Collections.unmodifiableMap(attributes));
		case XMLStreamConstants.END_ELEMENT:
			return new XMLEvent(XmlEventTypes.END_ELEMENT, parser.getLocalName(), null);
		case XMLStreamConstants.CHARACTERS:
			return new XMLEvent(XmlEventTypes.TEXT, parser.getText(), null);
		}
		throw new IllegalArgumentException("Weird token: "+token);
	}

}
