package fengfei.redis.example;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public class Monitor {
	public static void main(String[] args) throws InterruptedException {
		new Thread() {
			public void run() {
				long i = 0;
				while (true) {
					i++;
				}
			};
		}.start();
		OperatingSystemMXBean osBean = ManagementFactory
				.getPlatformMXBean(OperatingSystemMXBean.class);

		for (;;) {
			System.out.println(osBean.getSystemLoadAverage());
			System.out.printf("ProcessCpuLoad:%f, ProcessCpuTime:%d \n",
					osBean.getProcessCpuLoad()*100, osBean.getProcessCpuTime());
			System.out.printf("SystemCpuLoad:%f, SystemLoadAverage:%f \n",
					osBean.getSystemCpuLoad()*100, osBean.getSystemLoadAverage()*100);
			Thread.sleep(1000);
		}
	}
}
