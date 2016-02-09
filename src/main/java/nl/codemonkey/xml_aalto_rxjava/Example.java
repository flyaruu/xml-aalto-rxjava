package nl.codemonkey.xml_aalto_rxjava;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nl.codemonkey.xml_aalto_rxjava.XMLEvent.XmlEventTypes;


public class Example {

 
	public static void main(String[] args) throws InterruptedException, NumberFormatException, UnsupportedEncodingException {
		String city = "Amsterdam";
		HTTP.get("http://api.openweathermap.org/data/2.5/weather?q="+URLEncoder.encode(city,"UTF-8")+"&APPID=c9a22840a45f9da6f235c718475c4f08&mode=xml",1)
			// connect to endpoint, you can play around with the buffer size. If the size is 1, the bytes will be fed to the parser one at the time
		.doOnCompleted(()->System.err.println("Done"))
		.lift(XML.parse()) // The actual XML parsing, consumes a stream of byte[], emits a stream of XML Events
		.doOnNext(b->System.err.println(" XMLevent: - "+ b + " in thread: "+ Thread.currentThread().getName())) // Show some output
		.filter(e->e.getType()==XmlEventTypes.START_ELEMENT) // I'm only interested in START_ELEMENT events
		.filter(e->e.getText().equals("temperature")) // in tags that are called 'temperature'
		.first() // and I only need one
		.map(xml->Double.parseDouble(xml.getAttributes().get("value"))-273) // Convert to a double and convert from Kelvin to Celsius 
		.subscribe(
				temp->System.err.println("Temperature in Amsterdam: "+temp),
				exception->exception.printStackTrace()); // And show the result
		Thread.sleep(2000); // keep JVM alive for a bit
	}
}
