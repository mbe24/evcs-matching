package org.beyene.webapp.ev;

import org.beyene.protocol.api.EvApi;
import org.beyene.protocol.tcp.EvApiComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import picocli.CommandLine;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		CommandLine cmd = new CommandLine(options);
		cmd.setUsageHelpWidth(120);

		try {
			cmd.parse(args);
			if (cmd.isUsageHelpRequested()) {
				cmd.usage(System.out);
				return;
			} else if (cmd.isVersionHelpRequested()) {
				cmd.printVersionHelp(System.out);
				return;
			}

			new Application().runWith(options);
		} catch (CommandLine.ParameterException ex) {
			System.err.println(ex.getMessage());
			if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, System.err)) {
				ex.getCommandLine().usage(System.err);
			}
		}
	}

	public void runWith(Options options) throws Exception {
		SpringApplication application = new SpringApplication(Application.class);
		registerBeans(application, options);
		ConfigurableApplicationContext context = application.run();
	}

	private void registerBeans(SpringApplication application, Options options) {
		EvApi api = new EvApiComponent();
		application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean(EvApi.class, () -> api));
	}


}
