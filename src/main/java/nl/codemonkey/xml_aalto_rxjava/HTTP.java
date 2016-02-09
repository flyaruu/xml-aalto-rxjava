package nl.codemonkey.xml_aalto_rxjava;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import rx.Observable;

public class HTTP {

	// Quick and easy async HTTP client, using a proper non-blocking HTTP client like Apache HttpClient I leave as an exercise to the reader ;-)
	public static Observable<byte[]> get(String getUrl, int buffersize) {
		return Observable.create(subscriber->{
			Runnable r = new Runnable(){

				@Override
				public void run() {
					try {
						System.err.println("GET THREAD: "+Thread.currentThread().getName());
						URL url = new URL(getUrl);
						URLConnection conn = url.openConnection();
						InputStream is = conn.getInputStream();
						int nRead;
						byte[] data = new byte[buffersize];

						while ((nRead = is.read(data, 0, data.length)) != -1) {
							subscriber.onNext(Arrays.copyOfRange(data,0,nRead));
						}
						subscriber.onCompleted();
					} catch (Throwable e) {
						subscriber.onError(e);
					}
				}
			};
			// TODO use a threadpool?
			new Thread(r).start();
//			httpPool.execute(r);
//			r.run();
		});
	}
}
