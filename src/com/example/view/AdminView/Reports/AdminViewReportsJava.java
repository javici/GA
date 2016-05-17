package com.example.view.AdminView.Reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;

import com.example.CSVLoader.CSVLoader;
import com.example.Reports.TrimestralReports;
import com.example.Templates.MainContentView;
import com.example.view.AdminView.AdminView;
import com.example.view.AdminView.CSV.AdminViewCSVUploadJava.FileReciverStudents;
import com.example.view.AdminView.CSV.AdminViewCSVUploadJava.FileReciverTeachers;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.themes.ValoTheme;

public class AdminViewReportsJava extends MainContentView {
	private File file;
	private AdminReportCSVUpload csv;
	Button generateReportTrimestre1;
	StreamResource sr = getXLS();
	FileDownloader fileDownloader = new FileDownloader(sr);
	
	/*private FileReciverTrimestre2 receiver2 = new FileReciverTrimestre2();
	private FileReciverTrimestre3 receiver3 = new FileReciverTrimestre3();
	private FileReciverTotal receiver4 = new FileReciverTotal();*/
	
	public AdminViewReportsJava(){
		csv = new AdminReportCSVUpload();
		buttonsSettings();
		
		// INFORMES TRIMESTRALS
		
		csv.mainTrimestral.addStyleName("whiteBackground");
		csv.txtUpTrimestral.addStyleName("settings");
		csv.txtUpTrimestral.setValue("Carrega d'Informes Trimestrals");
		csv.hTrimestral.addStyleName("csvstudent");
		csv.hTrimestral.removeAllComponents();
		csv.hTrimestral.addComponent(generateReportTrimestre1);
	//	csv.horizontalTrimestral.setComponentAlignment(uploadStudent, Alignment.MIDDLE_CENTER);
		
		
		//INFORMES ANUALS

		csv.txtUpTotal.setValue("Carrega d'Informes Anuals");
		csv.hTotal.addStyleName("csvstudent");
	//	csv.hTotal.removeAllComponents();
	//	csv.hTotal.addComponents(uploadtrimestre1);

	//	csv.horizontalTotal.setComponentAlignment(uploadStudent, Alignment.MIDDLE_CENTER);
		fileDownloader.extend(generateReportTrimestre1);
		
	}
	
	
	
	

	private StreamResource getXLS() {
		// TODO Auto-generated method stub
	        StreamResource.StreamSource source = new StreamResource.StreamSource() {

	            public InputStream getStream() {
	                // return your file/bytearray as an InputStream
	            	 File excel = new File("/home/xmurcia/Escritorio/horesBUS.ods");
	            	 InputStream targetStream = null;
					try {
						targetStream = new FileInputStream(excel);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
					return targetStream;

	            }
	        };
	      StreamResource resource = new StreamResource ( source, getFileName());
	        return resource;
	}
	





	private String getFileName() {
		// TODO Auto-generated method stub
		
		String suggestedSaveFile = "Informe1erTrimestre.xls";
		
		return suggestedSaveFile;
	}





	private void buttonsSettings() {
		// TODO Auto-generated method stub
		generateReportTrimestre1 = new Button("Genera informe 1r trimestre", FontAwesome.CLOUD_DOWNLOAD);
		generateReportTrimestre1.setStyleName(ValoTheme.BUTTON_PRIMARY);
		generateReportTrimestre1.addStyleName("settings");
		
		vHorizontalMain.addComponent(csv);
		bAdd.setVisible(false);
		buttonEdit.setVisible(false);
		bDelete.setVisible(false);
		bRegister.setVisible(false);
		txtSearch.setVisible(false);
		clearTxt.setVisible(false);

		horizontalTitle.addStyleName("horizontal-title");
		txtTitle.addStyleName("main-title");
		txtTitle.setValue("Informes");

		// AdminViewCarregarCSVJava upload = new AdminViewCarregarCSVJava();

	}
	
	
	
	
	
}
