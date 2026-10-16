package com.example.BuiThiDiemTrinh_TH5;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BuiThiDiemTrinhTh5Application {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(BuiThiDiemTrinhTh5Application.class);
		boolean guiEnabled = Arrays.stream(args)
				.anyMatch(argument -> argument.equals("--app.gui.enabled")
						|| argument.equals("--app.gui.enabled=true"));
		application.setHeadless(!guiEnabled);
		application.run(args);
	}

}
