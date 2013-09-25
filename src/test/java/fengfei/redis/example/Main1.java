package fengfei.redis.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class Main1 extends Thread {
	static volatile int value;
	static AtomicLong al = new AtomicLong(0);
	public long pv = 0;
	CountDownLatch latch;

	public Main1(CountDownLatch latch) {
		super();
		this.latch = latch;
	}

	public static void main(String[] args) {
		CountDownLatch latch = new CountDownLatch(20);
		Main1 ts[] = new Main1[20];
		for (int i = 0; i < ts.length; i++) {
			ts[i] = new Main1(latch);
		}
		for (int i = 0; i < ts.length; i++) {
			ts[i].start();
		}
		try {
			latch.await();
			long x = 0;
			for (int i = 0; i < ts.length; i++) {
				x += ts[i].pv;
			}
			System.out.println(x + "   " + value + "   " + al.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	static Object lock = new Object();

	@Override
	public void run() {
		while (pv <= 6000) {
			synchronized (lock) {
				value++;
			}

			al.getAndIncrement();
			pv++;

		}
		latch.countDown();
	}

}
