/*******************************************************************************
 * 
 * Gestió d'Amonestacions v1.0
 *
 * Esta obra está sujeta a la licencia Reconocimiento-NoComercial-SinObraDerivada 4.0 Internacional de Creative Commons. 
 * Para ver una copia de esta licencia, visite http://creativecommons.org/licenses/by-nc-nd/4.0/.
 *  
 * @author Francisco Javier Casado Moreno - fcasado@elpuig.xeill.net 
 * @author Daniel Pérez Palacino - dperez@elpuig.xeill.net 
 * @author Gerard Enrique Paulino Decena - gpaulino@elpuig.xeill.net 
 * @author Xavier Murcia Gámez - xmurcia@elpuig.xeill.net 
 * 
 *******************************************************************************/
package com.example.Reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.persistence.Query;

import com.example.Dates.ConverterDates;
import com.example.Entities.Group;
import com.example.Logic.GroupJPAManager;
import com.google.gwt.user.client.rpc.core.java.util.Collections;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.FileResource;

public class TrimestralReports {
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "ALUMNE,A,E";

	public ReportQuerys query;
	ArrayList<Date> dates = new ArrayList<Date>();
	// CSV file header
	public  GroupJPAManager jpa;
	private  List<Group> grupos = null;
	private ResourceBundle rb = ResourceBundle.getBundle("GA");

	ConverterDates datas;
	public void calcularPrimerTrimestre() {
		datas = new ConverterDates();
		// CALCULO DE FECHAS
		// VARIABLE A COJER
		try {
			dates = readFile();
		} catch (ReadOnlyException e1) {
			e1.printStackTrace();
		} catch (ConversionException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Date firstTrimesterStartDate;
		Calendar startDate;

		List warningCalculate;
		List pushOutCalculate;
		Date firstTrimesterFinalDate;

		long diff;
		long numberOfWeeks;

		FileWriter fileWriter = null;

		jpa = new GroupJPAManager();
		grupos = new ArrayList<>();
		grupos = jpa.getGroups();

		for (int x = 0; x < grupos.size(); x++) {

			firstTrimesterStartDate = dates.get(0);
			startDate = Calendar.getInstance();
			startDate.setTime(firstTrimesterStartDate);

			warningCalculate = null;
			pushOutCalculate = null;
			firstTrimesterFinalDate = dates.get(1);

			diff = firstTrimesterFinalDate.getTime() - firstTrimesterStartDate.getTime();
			numberOfWeeks = (diff / (24 * 60 * 60 * 1000)) / 7;

			// FOR STUDENT ID
			query = new ReportQuerys();
			List ids = query.getIdAlumnes(grupos.get(x).getId());

			// query.closeTransaction();

			List idList = new ArrayList<>();

			for (int i = 0; i < ids.size(); i++) {
				idList.add(ids.get(i));

				System.out.println(ids.get(i));
			}

			// FOR NOMS
			query = new ReportQuerys();
			List names = query.getNomAlumnes(grupos.get(x).getId());
			// query.closeTransaction();

			List namesList = new ArrayList<>();

			for (int i = 0; i < names.size(); i++) {
				namesList.add(names.get(i));

			}

			// FOR COGNOMS

			query = new ReportQuerys();
			List surnames = query.getCognomsAlumnes(grupos.get(x).getId());
			// query.closeTransaction();

			List surnamesList = new ArrayList<>();

			for (int i = 0; i < surnames.size(); i++) {
				surnamesList.add(surnames.get(i));

			}

			try {

				File f = new File("/tmp/trimestre1");
				if (!f.exists()) {
					f.mkdirs();
				}

				fileWriter = new FileWriter("/tmp/trimestre1/alumnes" + grupos.get(x).getId() + ".xls");
				query = new ReportQuerys();
				String dateCurs = query.getDateCurs();
				// query.closeTransaction();

				fileWriter.append("1r Trimestre   Curs: " + dateCurs);
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(NEW_LINE_SEPARATOR);
				fileWriter.append(NEW_LINE_SEPARATOR);

				String grup = grupos.get(x).getId();
				fileWriter.append(grup);
				fileWriter.append(COMMA_DELIMITER);

				// CONSULTA
				String week = "Set. ";

				for (int i = 1; i <= numberOfWeeks; i++) {

					week.equals(numberOfWeeks + i);
					fileWriter.append(week + i);
					fileWriter.append(COMMA_DELIMITER);

					Date diaInicial = startDate.getTime();
					String fechainicialBuena = datas.converterDate(diaInicial);

					fileWriter.append(fechainicialBuena);
					// diaInici=aux;
					fileWriter.append(COMMA_DELIMITER);
					startDate.add(Calendar.DATE, 7);

				}
				fileWriter.append(NEW_LINE_SEPARATOR);

				// Headers
				fileWriter.append("ALUMNE");
				fileWriter.append(COMMA_DELIMITER);

				for (int i = 0; i < numberOfWeeks; i++) {
					fileWriter.append("A");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("E");
					fileWriter.append(COMMA_DELIMITER);

				}

				// fileWriter.append(COMMA_DELIMITER);

				fileWriter.append(NEW_LINE_SEPARATOR);

				// ADD STUDENTS
				for (int i = 0; i < namesList.size(); i++) {
					Calendar startWeekDay = Calendar.getInstance();
					startWeekDay.setTime(firstTrimesterStartDate);
					Date week2 = null;
					Date week1 = startWeekDay.getTime();
					startWeekDay.add(Calendar.DATE, 7);

					week2 = startWeekDay.getTime();
					fileWriter.append(namesList.get(i).toString() + " " + surnamesList.get(i).toString());
					fileWriter.append(COMMA_DELIMITER);

					for (int l = 0; l < numberOfWeeks; l++) {

						warningCalculate = new ArrayList<>();
						pushOutCalculate = new ArrayList<>();

						// Debería de pasarle solo el id del alumnno
						warningCalculate = calcularAmonestadosPorSemana(idList, week1, week2);
						pushOutCalculate = calcularExpulsadosPorSemana(idList, week1, week2);

						week1 = week2;
						startWeekDay.add(Calendar.DATE, 7);
						week2 = startWeekDay.getTime();

						if (warningCalculate.get(i).toString().equals("0")) {
							fileWriter.append("");

						} else {
							fileWriter.append(warningCalculate.get(i).toString());

						}
						fileWriter.append(COMMA_DELIMITER);

						if (pushOutCalculate.get(i).toString().equals("0")) {
							fileWriter.append("");

						} else {
							fileWriter.append(pushOutCalculate.get(i).toString());

						}
						fileWriter.append(COMMA_DELIMITER);
					}

					fileWriter.append(NEW_LINE_SEPARATOR);

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Grup " + grupos.get(x).getId() + "finalitzat");

		}
	}

	public  void calculateSecondTrimester() {
		// CALCULO DE FECHAS
		// VARIABLE A COJER
		try {
			dates = readFile();
		} catch (ReadOnlyException e1) {
			e1.printStackTrace();
		} catch (ConversionException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Date secondTrimesterStartDate;
		Calendar calendarStartDate;
		List calculoAmonest = null;
		List calculoExpuls = null;
		Date diaFinalTrimestre2;

		long diff;
		long numSetmanes;
		// Delimiter used in CSV file

		FileWriter fileWriter = null;

		// Create new students objects
		jpa = new GroupJPAManager();
		grupos = new ArrayList<>();
		grupos = jpa.getGroups();

		for (int x = 0; x < grupos.size(); x++) {
			secondTrimesterStartDate = dates.get(2);
			calendarStartDate = Calendar.getInstance();
			calendarStartDate.setTime(secondTrimesterStartDate);

			calculoAmonest = null;
			calculoExpuls = null;
			diaFinalTrimestre2 = dates.get(3);

			diff = diaFinalTrimestre2.getTime() - secondTrimesterStartDate.getTime();
			numSetmanes = (diff / (24 * 60 * 60 * 1000)) / 7;

			// FOR STUDENT ID
			query = new ReportQuerys();
			List ids = query.getIdAlumnes(grupos.get(x).getId());

			// query.closeTransaction();

			List idList = new ArrayList<>();

			for (int i = 0; i < ids.size(); i++) {
				idList.add(ids.get(i));

				// System.out.println(ids.get(i));
			}

			// FOR NOMS
			query = new ReportQuerys();
			List noms = query.getNomAlumnes(grupos.get(x).getId());
			// query.closeTransaction();

			List nomsList = new ArrayList<>();

			for (int i = 0; i < noms.size(); i++) {
				nomsList.add(noms.get(i));

			}

			// FOR COGNOMS

			query = new ReportQuerys();
			List cognoms = query.getCognomsAlumnes(grupos.get(x).getId());
			// query.closeTransaction();

			List cognomsList = new ArrayList<>();

			for (int i = 0; i < cognoms.size(); i++) {
				cognomsList.add(cognoms.get(i));

			}

			try {
				File f = new File("/tmp/trimestre2");
				if (!f.exists()) {
					f.mkdirs();
				}

				fileWriter = new FileWriter("/tmp/trimestre2/alumnes" + grupos.get(x).getId() + ".xls");
				query = new ReportQuerys();
				String dateCurs = query.getDateCurs();
				// query.closeTransaction();

				fileWriter.append("2r Trimestre   Curs: " + dateCurs);
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(NEW_LINE_SEPARATOR);
				fileWriter.append(NEW_LINE_SEPARATOR);

				String grup = grupos.get(x).getId();
				fileWriter.append(grup);
				fileWriter.append(COMMA_DELIMITER);

				// CONSULTA
				String setmana = "Set. ";

				for (int i = 1; i <= numSetmanes; i++) {

					setmana.equals(numSetmanes + i);
					fileWriter.append(setmana + i);
					fileWriter.append(COMMA_DELIMITER);

					Date diaInicial = calendarStartDate.getTime();
					String fechainicialBuena = datas.converterDate(diaInicial);

					fileWriter.append(fechainicialBuena);
					fileWriter.append(COMMA_DELIMITER);
					calendarStartDate.add(Calendar.DATE, 7);

				}
				fileWriter.append(NEW_LINE_SEPARATOR);

				// Headers
				fileWriter.append("ALUMNE");
				fileWriter.append(COMMA_DELIMITER);

				for (int i = 0; i < numSetmanes; i++) {
					fileWriter.append("A");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("E");
					fileWriter.append(COMMA_DELIMITER);

				}

				fileWriter.append(NEW_LINE_SEPARATOR);

				// ADD STUDENTS
				for (int i = 0; i < nomsList.size(); i++) {
					Calendar diaInicisetmanes = Calendar.getInstance();
					diaInicisetmanes.setTime(secondTrimesterStartDate);
					Date semana2 = null;
					Date semana1 = diaInicisetmanes.getTime();
					diaInicisetmanes.add(Calendar.DATE, 7);

					semana2 = diaInicisetmanes.getTime();
					fileWriter.append(nomsList.get(i).toString() + " " + cognomsList.get(i).toString());
					fileWriter.append(COMMA_DELIMITER);

					for (int l = 0; l < numSetmanes; l++) {

						calculoExpuls = new ArrayList<>();
						calculoAmonest = new ArrayList<>();

						calculoAmonest = calcularAmonestadosPorSemana(idList, semana1, semana2);
						calculoExpuls = calcularExpulsadosPorSemana(idList, semana1, semana2);

						semana1 = semana2;
						diaInicisetmanes.add(Calendar.DATE, 7);
						semana2 = diaInicisetmanes.getTime();

						if (calculoAmonest.get(i).toString().equals("0")) {
							fileWriter.append("");

						} else {
							fileWriter.append(calculoAmonest.get(i).toString());

						}
						fileWriter.append(COMMA_DELIMITER);

						if (calculoExpuls.get(i).toString().equals("0")) {
							fileWriter.append("");

						} else {
							fileWriter.append(calculoExpuls.get(i).toString());

						}
						fileWriter.append(COMMA_DELIMITER);
					}

					fileWriter.append(NEW_LINE_SEPARATOR);

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Grup " + grupos.get(x).getId() + "finalitzat");

		}
	}

	public  void calculateThirdTrimester() {
		// CALCULO DE FECHAS
		// VARIABLE A COJER
		try {
			dates = readFile();
		} catch (ReadOnlyException e1) {
			e1.printStackTrace();
		} catch (ConversionException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Date thirdTrimesterStartDate;
		Calendar startDay;
		List warningCalculate = null;
		List pushOutCalculate = null;
		Date thirdTrimestralFinalDate;

		long diff;
		long numberOfWeeks;
		// Delimiter used in CSV file

		FileWriter fileWriter = null;

		// Create new students objects
		jpa = new GroupJPAManager();
		grupos = new ArrayList<>();
		grupos = jpa.getGroups();

		for (int x = 0; x < grupos.size(); x++) {
			thirdTrimesterStartDate = dates.get(4);
			startDay = Calendar.getInstance();
			startDay.setTime(thirdTrimesterStartDate);

			warningCalculate = null;
			pushOutCalculate = null;
			thirdTrimestralFinalDate = dates.get(5);

			diff = thirdTrimestralFinalDate.getTime() - thirdTrimesterStartDate.getTime();
			numberOfWeeks = (diff / (24 * 60 * 60 * 1000)) / 7;

			// FOR STUDENT ID
			query = new ReportQuerys();
			List ids = query.getIdAlumnes(grupos.get(x).getId());

			// query.closeTransaction();

			List idList = new ArrayList<>();

			for (int i = 0; i < ids.size(); i++) {
				idList.add(ids.get(i));

				// System.out.println(ids.get(i));
			}

			// FOR NOMS
			query = new ReportQuerys();
			List noms = query.getNomAlumnes(grupos.get(x).getId());
			// query.closeTransaction();

			List nomsList = new ArrayList<>();

			for (int i = 0; i < noms.size(); i++) {
				nomsList.add(noms.get(i));

			}

			// FOR COGNOMS

			query = new ReportQuerys();
			List cognoms = query.getCognomsAlumnes(grupos.get(x).getId());
			// query.closeTransaction();

			List cognomsList = new ArrayList<>();

			for (int i = 0; i < cognoms.size(); i++) {
				cognomsList.add(cognoms.get(i));

			}

			try {

				File f = new File("/tmp/trimestre3");
				if (!f.exists()) {
					f.mkdirs();
				}

				fileWriter = new FileWriter("/tmp/trimestre3/alumnes" + grupos.get(x).getId() + ".xls");
				query = new ReportQuerys();
				String dateCurs = query.getDateCurs();
				// query.closeTransaction();

				fileWriter.append("3r Trimestre   Curs: " + dateCurs);
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(NEW_LINE_SEPARATOR);
				fileWriter.append(NEW_LINE_SEPARATOR);

				String grup = grupos.get(x).getId();
				fileWriter.append(grup);
				fileWriter.append(COMMA_DELIMITER);

				// CONSULTA
				String setmana = "Set. ";

				for (int i = 1; i <= numberOfWeeks; i++) {

					setmana.equals(numberOfWeeks + i);
					fileWriter.append(setmana + i);
					fileWriter.append(COMMA_DELIMITER);

					Date diaInicial = startDay.getTime();
					String fechainicialBuena = datas.converterDate(diaInicial);

					fileWriter.append(fechainicialBuena);
					fileWriter.append(COMMA_DELIMITER);
					startDay.add(Calendar.DATE, 7);

				}
				fileWriter.append(NEW_LINE_SEPARATOR);

				// Headers
				fileWriter.append("ALUMNE");
				fileWriter.append(COMMA_DELIMITER);

				for (int i = 0; i < numberOfWeeks; i++) {
					fileWriter.append("A");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("E");
					fileWriter.append(COMMA_DELIMITER);

				}

				fileWriter.append(NEW_LINE_SEPARATOR);

				// ADD STUDENTS
				for (int i = 0; i < nomsList.size(); i++) {
					Calendar diaInicisetmanes = Calendar.getInstance();
					diaInicisetmanes.setTime(thirdTrimesterStartDate);
					Date semana2 = null;
					Date semana1 = diaInicisetmanes.getTime();
					diaInicisetmanes.add(Calendar.DATE, 7);

					semana2 = diaInicisetmanes.getTime();
					fileWriter.append(nomsList.get(i).toString() + " " + cognomsList.get(i).toString());
					fileWriter.append(COMMA_DELIMITER);

					for (int l = 0; l < numberOfWeeks; l++) {

						pushOutCalculate = new ArrayList<>();
						warningCalculate = new ArrayList<>();

						warningCalculate = calcularAmonestadosPorSemana(idList, semana1, semana2);
						pushOutCalculate = calcularExpulsadosPorSemana(idList, semana1, semana2);

						semana1 = semana2;
						diaInicisetmanes.add(Calendar.DATE, 7);
						semana2 = diaInicisetmanes.getTime();

						if (warningCalculate.get(i).toString().equals("0")) {
							fileWriter.append("");

						} else {
							fileWriter.append(warningCalculate.get(i).toString());

						}
						fileWriter.append(COMMA_DELIMITER);

						if (pushOutCalculate.get(i).toString().equals("0")) {
							fileWriter.append("");

						} else {
							fileWriter.append(pushOutCalculate.get(i).toString());

						}
						fileWriter.append(COMMA_DELIMITER);
					}

					fileWriter.append(NEW_LINE_SEPARATOR);

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Grup " + grupos.get(x).getId() + "finalitzat");

		}
	}

	private List calcularAmonestadosPorSemana(List idList, Date semana1, Date semana2) {

		List amonestacions1;

		amonestacions1 = new ArrayList<>();

		for (int j = 0; j < idList.size(); j++) {
			query = new ReportQuerys();
			amonestacions1.add(query.getWarningCurs(Integer.parseInt(idList.get(j).toString()), semana1, semana2));
			// query.closeTransaction();

		}

		return amonestacions1;

	}

	private List calcularExpulsadosPorSemana(List idList, Date semana1, Date semana2) {

		List expulsions1;
		List expulsionsList1 = null;

		expulsions1 = new ArrayList<>();

		for (int j = 0; j < idList.size(); j++) {
			query = new ReportQuerys();
			expulsions1.add(query.getExpulsionCurs(Integer.parseInt(idList.get(j).toString()), semana1, semana2));
			// query.closeTransaction();

		}

		return expulsions1;
	}

	public void calcularResumenTrimestre1() {
		FileWriter fileWriter = null;

		Date diaIniciTrimestre1;
		Calendar diaIniciCal;
		List calculoAmonest;
		List calculoExpuls;
		Date diaFinalTrimestre1;
		long diff;
		long numSetmanes;
		int totalAmonest = 0;
		int totalExpuls = 0;
		try {

			dates = readFile();
			File f2 = new File("/tmp/trimestre1");
			if (!f2.exists()) {
				f2.mkdirs();
			}
			fileWriter = new FileWriter("/tmp/trimestre1/resumen.xls");
			query = new ReportQuerys();
			String dateCurs = query.getDateCurs();
			// query.closeTransaction();

			jpa = new GroupJPAManager();
			grupos = new ArrayList<>();
			grupos = jpa.getGroups();
			// jpa.closeTransaction();

			diaIniciTrimestre1 = dates.get(0);
			diaIniciCal = Calendar.getInstance();
			diaIniciCal.setTime(diaIniciTrimestre1);
			diaFinalTrimestre1 = dates.get(1);
			diff = diaFinalTrimestre1.getTime() - diaIniciTrimestre1.getTime();
			numSetmanes = (diff / (24 * 60 * 60 * 1000)) / 7;

			calculoAmonest = null;
			calculoExpuls = null;

			diaFinalTrimestre1 = dates.get(1);
			fileWriter.append("1r Trimestre   Curs: " + dateCurs);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Headers

			// CONSULTA
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(COMMA_DELIMITER);

			String setmana = "Set. ";

			for (int i = 1; i <= numSetmanes; i++) {

				setmana.equals(numSetmanes + i);
				fileWriter.append(setmana + i);
				fileWriter.append(COMMA_DELIMITER);

				Date diaInicial = diaIniciCal.getTime();
				String fechainicialBuena = datas.converterDate(diaInicial);

				fileWriter.append(fechainicialBuena);
				fileWriter.append(COMMA_DELIMITER);
				diaIniciCal.add(Calendar.DATE, 7);

			}
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append("GRUP");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Nº ALUMNES");

			for (int i = 0; i < numSetmanes; i++) {
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("A");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("E");

			}

			fileWriter.append(NEW_LINE_SEPARATOR);

			for (int i = 0; i < grupos.size(); i++) {
				fileWriter.append(grupos.get(i).getId());
				fileWriter.append(COMMA_DELIMITER);

				// FOR STUDENT ID
				query = new ReportQuerys();
				List ids = query.getIdAlumnes(grupos.get(i).getId());

				// query.closeTransaction();

				List idList = new ArrayList<>();

				for (int j = 0; j < ids.size(); j++) {
					idList.add(ids.get(j));

					// System.out.println(ids.get(j));
				}
				fileWriter.append(String.valueOf(idList.size()));
				fileWriter.append(COMMA_DELIMITER);

				Calendar diaInicisetmanes = Calendar.getInstance();
				diaInicisetmanes.setTime(diaIniciTrimestre1);
				Date semana2 = null;
				Date semana1 = diaInicisetmanes.getTime();
				diaInicisetmanes.add(Calendar.DATE, 7);

				semana2 = diaInicisetmanes.getTime();

				/////////////////////////////
				for (int l = 0; l < numSetmanes; l++) {
					totalAmonest = 0;
					totalExpuls = 0;
					calculoExpuls = new ArrayList<>();
					calculoAmonest = new ArrayList<>();

					calculoAmonest = calcularAmonestadosPorSemana(idList, semana1, semana2);
					calculoExpuls = calcularExpulsadosPorSemana(idList, semana1, semana2);

					for (int n = 0; n < calculoAmonest.size(); n++) {
						totalAmonest = totalAmonest + Integer.parseInt(calculoAmonest.get(n).toString());
					}

					for (int n = 0; n < calculoExpuls.size(); n++) {
						totalExpuls = totalExpuls + Integer.parseInt(calculoExpuls.get(n).toString());
					}

					semana1 = semana2;
					diaInicisetmanes.add(Calendar.DATE, 7);
					semana2 = diaInicisetmanes.getTime();

					if (totalAmonest == 0) {
						fileWriter.append("");

					} else {
						fileWriter.append(String.valueOf(totalAmonest));

					}
					fileWriter.append(COMMA_DELIMITER);

					if (totalExpuls == 0) {
						fileWriter.append("");

					} else {
						fileWriter.append(String.valueOf(totalExpuls));

					}
					fileWriter.append(COMMA_DELIMITER);
				}
				////////////////////////////////////

				fileWriter.append(NEW_LINE_SEPARATOR);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void calcularResumenTrimestre2() {
		FileWriter fileWriter = null;

		Date diaIniciTrimestre2;
		Calendar diaIniciCal;
		List calculoAmonest = null;
		List calculoExpuls = null;
		Date diaFinalTrimestre2;
		long diff;
		long numSetmanes;
		int totalAmonest = 0;
		int totalExpuls = 0;
		try {

			dates = readFile();
			File f = new File("/tmp/trimestre2");
			if (!f.exists()) {
				f.mkdirs();
			}
			fileWriter = new FileWriter("/tmp/trimestre2/resumen.xls");
			query = new ReportQuerys();
			String dateCurs = query.getDateCurs();
			// query.closeTransaction();

			jpa = new GroupJPAManager();
			grupos = new ArrayList<>();
			grupos = jpa.getGroups();
			// jpa.closeTransaction();

			diaIniciTrimestre2 = dates.get(2);
			diaIniciCal = Calendar.getInstance();
			diaIniciCal.setTime(diaIniciTrimestre2);

			calculoAmonest = null;
			calculoExpuls = null;
			diaFinalTrimestre2 = dates.get(3);

			diff = diaFinalTrimestre2.getTime() - diaIniciTrimestre2.getTime();
			numSetmanes = (diff / (24 * 60 * 60 * 1000)) / 7;

			fileWriter.append("1r Trimestre   Curs: " + dateCurs);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Headers

			// CONSULTA
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(COMMA_DELIMITER);

			String setmana = "Set. ";

			for (int i = 1; i <= numSetmanes; i++) {

				setmana.equals(numSetmanes + i);
				fileWriter.append(setmana + i);
				fileWriter.append(COMMA_DELIMITER);

				Date diaInicial = diaIniciCal.getTime();
				String fechainicialBuena = datas.converterDate(diaInicial);

				fileWriter.append(fechainicialBuena);
				fileWriter.append(COMMA_DELIMITER);
				diaIniciCal.add(Calendar.DATE, 7);

			}
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append("GRUP");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Nº ALUMNES");

			for (int i = 0; i < numSetmanes; i++) {
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("A");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("E");

			}

			fileWriter.append(NEW_LINE_SEPARATOR);

			for (int i = 0; i < grupos.size(); i++) {
				fileWriter.append(grupos.get(i).getId());
				fileWriter.append(COMMA_DELIMITER);

				// FOR STUDENT ID
				query = new ReportQuerys();
				List ids = query.getIdAlumnes(grupos.get(i).getId());

				// query.closeTransaction();

				List idList = new ArrayList<>();

				for (int j = 0; j < ids.size(); j++) {
					idList.add(ids.get(j));

					// System.out.println(ids.get(j));
				}
				fileWriter.append(String.valueOf(idList.size()));
				fileWriter.append(COMMA_DELIMITER);

				Calendar diaInicisetmanes = Calendar.getInstance();
				diaInicisetmanes.setTime(diaIniciTrimestre2);
				Date semana2 = null;
				Date semana1 = diaInicisetmanes.getTime();
				diaInicisetmanes.add(Calendar.DATE, 7);

				semana2 = diaInicisetmanes.getTime();

				/////////////////////////////
				for (int l = 0; l < numSetmanes; l++) {
					totalAmonest = 0;
					totalExpuls = 0;
					calculoExpuls = new ArrayList<>();
					calculoAmonest = new ArrayList<>();

					calculoAmonest = calcularAmonestadosPorSemana(idList, semana1, semana2);
					calculoExpuls = calcularExpulsadosPorSemana(idList, semana1, semana2);

					for (int n = 0; n < calculoAmonest.size(); n++) {
						totalAmonest = totalAmonest + Integer.parseInt(calculoAmonest.get(n).toString());
					}

					for (int n = 0; n < calculoExpuls.size(); n++) {
						totalExpuls = totalExpuls + Integer.parseInt(calculoExpuls.get(n).toString());
					}

					semana1 = semana2;
					diaInicisetmanes.add(Calendar.DATE, 7);
					semana2 = diaInicisetmanes.getTime();

					if (totalAmonest == 0) {
						fileWriter.append("");

					} else {
						fileWriter.append(String.valueOf(totalAmonest));

					}
					fileWriter.append(COMMA_DELIMITER);

					if (totalExpuls == 0) {
						fileWriter.append("");

					} else {
						fileWriter.append(String.valueOf(totalExpuls));

					}
					fileWriter.append(COMMA_DELIMITER);
				}
				////////////////////////////////////

				fileWriter.append(NEW_LINE_SEPARATOR);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void calcularResumenTrimestre3() {
		FileWriter fileWriter = null;

		Date diaIniciTrimestre3;
		Calendar diaIniciCal;
		List calculoAmonest = null;
		List calculoExpuls = null;
		Date diaFinalTrimestre3;
		long diff;
		long numSetmanes;
		int totalAmonest = 0;
		int totalExpuls = 0;
		try {

			dates = readFile();
			File f = new File("/tmp/trimestre3");
			if (!f.exists()) {
				f.mkdirs();
			}
			fileWriter = new FileWriter("/tmp/trimestre3/resumen.xls");
			query = new ReportQuerys();
			String dateCurs = query.getDateCurs();
			// query.closeTransaction();

			jpa = new GroupJPAManager();
			grupos = new ArrayList<>();
			grupos = jpa.getGroups();
			// jpa.closeTransaction();

			diaIniciTrimestre3 = dates.get(4);
			diaIniciCal = Calendar.getInstance();
			diaIniciCal.setTime(diaIniciTrimestre3);

			calculoAmonest = null;
			calculoExpuls = null;
			diaFinalTrimestre3 = dates.get(5);

			diff = diaFinalTrimestre3.getTime() - diaIniciTrimestre3.getTime();
			numSetmanes = (diff / (24 * 60 * 60 * 1000)) / 7;

			fileWriter.append("1r Trimestre   Curs: " + dateCurs);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Headers

			// CONSULTA
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(COMMA_DELIMITER);

			String setmana = "Set. ";

			for (int i = 1; i <= numSetmanes; i++) {

				setmana.equals(numSetmanes + i);
				fileWriter.append(setmana + i);
				fileWriter.append(COMMA_DELIMITER);

				Date diaInicial = diaIniciCal.getTime();
				String fechainicialBuena = datas.converterDate(diaInicial);

				fileWriter.append(fechainicialBuena);
				fileWriter.append(COMMA_DELIMITER);
				diaIniciCal.add(Calendar.DATE, 7);

			}
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append("GRUP");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Nº ALUMNES");

			for (int i = 0; i < numSetmanes; i++) {
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("A");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("E");

			}

			fileWriter.append(NEW_LINE_SEPARATOR);

			for (int i = 0; i < grupos.size(); i++) {
				fileWriter.append(grupos.get(i).getId());
				fileWriter.append(COMMA_DELIMITER);

				// FOR STUDENT ID
				query = new ReportQuerys();
				List ids = query.getIdAlumnes(grupos.get(i).getId());

				// query.closeTransaction();

				List idList = new ArrayList<>();

				for (int j = 0; j < ids.size(); j++) {
					idList.add(ids.get(j));

					// System.out.println(ids.get(j));
				}
				fileWriter.append(String.valueOf(idList.size()));
				fileWriter.append(COMMA_DELIMITER);

				Calendar diaInicisetmanes = Calendar.getInstance();
				diaInicisetmanes.setTime(diaIniciTrimestre3);
				Date semana2 = null;
				Date semana1 = diaInicisetmanes.getTime();
				diaInicisetmanes.add(Calendar.DATE, 7);

				semana2 = diaInicisetmanes.getTime();

				/////////////////////////////
				for (int l = 0; l < numSetmanes; l++) {
					totalAmonest = 0;
					totalExpuls = 0;
					calculoExpuls = new ArrayList<>();
					calculoAmonest = new ArrayList<>();

					calculoAmonest = calcularAmonestadosPorSemana(idList, semana1, semana2);
					calculoExpuls = calcularExpulsadosPorSemana(idList, semana1, semana2);

					for (int n = 0; n < calculoAmonest.size(); n++) {
						totalAmonest = totalAmonest + Integer.parseInt(calculoAmonest.get(n).toString());
					}

					for (int n = 0; n < calculoExpuls.size(); n++) {
						totalExpuls = totalExpuls + Integer.parseInt(calculoExpuls.get(n).toString());
					}

					semana1 = semana2;
					diaInicisetmanes.add(Calendar.DATE, 7);
					semana2 = diaInicisetmanes.getTime();

					if (totalAmonest == 0) {
						fileWriter.append("");

					} else {
						fileWriter.append(String.valueOf(totalAmonest));

					}
					fileWriter.append(COMMA_DELIMITER);

					if (totalExpuls == 0) {
						fileWriter.append("");

					} else {
						fileWriter.append(String.valueOf(totalExpuls));

					}
					fileWriter.append(COMMA_DELIMITER);
				}
				////////////////////////////////////

				fileWriter.append(NEW_LINE_SEPARATOR);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void calcularResumen2Trimestre1() {
		FileWriter fileWriter = null;

		Date diaIniciTrimestre1;
		Calendar diaIniciCal;
		List calculoAmonest;
		List calculoExpuls;
		Date diaFinalTrimestre1;
		long diff;
		long numSetmanes;
		int totalAmonest = 0;
		int totalExpuls = 0;
		int total = 0;
		float mediaParteAlumnoGrupo = 0;

		try {

			dates = readFile();
			File f = new File("/tmp/trimestre1");
			if (!f.exists()) {
				f.mkdirs();
			}
			fileWriter = new FileWriter("/tmp/trimestre1/resumen2.xls");
			query = new ReportQuerys();
			String dateCurs = query.getDateCurs();
			// query.closeTransaction();

			jpa = new GroupJPAManager();
			grupos = new ArrayList<>();
			grupos = jpa.getGroups();
			// jpa.closeTransaction();

			diaIniciTrimestre1 = dates.get(0);
			diaIniciCal = Calendar.getInstance();
			diaIniciCal.setTime(diaIniciTrimestre1);
			diaFinalTrimestre1 = dates.get(1);
			diff = diaFinalTrimestre1.getTime() - diaIniciTrimestre1.getTime();
			numSetmanes = (diff / (24 * 60 * 60 * 1000)) / 7;

			calculoAmonest = null;
			calculoExpuls = null;

			fileWriter.append("1r Trimestre   Curs: " + dateCurs);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Headers

			// CONSULTA
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(COMMA_DELIMITER);

			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append("GRUP");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Nº ALUMNES");

			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("A");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("E");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("TOTAL");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Partes per alumne i grup");
			fileWriter.append(COMMA_DELIMITER);

			fileWriter.append(NEW_LINE_SEPARATOR);

			for (int i = 0; i < grupos.size(); i++) {
				fileWriter.append(grupos.get(i).getId());
				fileWriter.append(COMMA_DELIMITER);

				// FOR STUDENT ID
				query = new ReportQuerys();
				List ids = query.getIdAlumnes(grupos.get(i).getId());

				// query.closeTransaction();

				List idList = new ArrayList<>();

				for (int j = 0; j < ids.size(); j++) {
					idList.add(ids.get(j));

					// System.out.println(ids.get(j));
				}
				fileWriter.append(String.valueOf(idList.size()));
				fileWriter.append(COMMA_DELIMITER);

				total = 0;
				totalAmonest = 0;
				totalExpuls = 0;
				mediaParteAlumnoGrupo = 0;
				/////////////////////////////

				calculoExpuls = new ArrayList<>();
				calculoAmonest = new ArrayList<>();

				calculoAmonest = calcularAmonestadosPorSemana(idList, diaIniciTrimestre1, diaFinalTrimestre1);
				calculoExpuls = calcularExpulsadosPorSemana(idList, diaIniciTrimestre1, diaFinalTrimestre1);

				for (int n = 0; n < calculoAmonest.size(); n++) {
					totalAmonest = totalAmonest + Integer.parseInt(calculoAmonest.get(n).toString());
				}

				for (int n = 0; n < calculoExpuls.size(); n++) {
					totalExpuls = totalExpuls + Integer.parseInt(calculoExpuls.get(n).toString());
				}

				total = totalExpuls + totalAmonest;
				mediaParteAlumnoGrupo = ((total / 1024.0f) * 255) / ((idList.size() / 1024.0f) * 255);

				////////////////////////////////////
				if (totalAmonest == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(totalAmonest));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (totalExpuls == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(totalExpuls));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (total == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(total));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (mediaParteAlumnoGrupo == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(Float.toString(mediaParteAlumnoGrupo));

				}
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(NEW_LINE_SEPARATOR);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void calcularResumen2Trimestre2() {
		FileWriter fileWriter = null;

		Date diaIniciTrimestre2;
		Calendar diaIniciCal;
		List calculoAmonest;
		List calculoExpuls;
		Date diaFinalTrimestre2;
		long diff;
		long numSetmanes;
		int totalAmonest = 0;
		int totalExpuls = 0;
		int total = 0;
		float mediaParteAlumnoGrupo = 0;

		try {

			dates = readFile();
			File f = new File("/tmp/trimestre2");
			if (!f.exists()) {
				f.mkdirs();
			}
			fileWriter = new FileWriter("/tmp/trimestre2/resumen2.xls");
			query = new ReportQuerys();
			String dateCurs = query.getDateCurs();
			// query.closeTransaction();

			jpa = new GroupJPAManager();
			grupos = new ArrayList<>();
			grupos = jpa.getGroups();
			// jpa.closeTransaction();

			diaIniciTrimestre2 = dates.get(2);
			diaIniciCal = Calendar.getInstance();
			diaIniciCal.setTime(diaIniciTrimestre2);
			diaFinalTrimestre2 = dates.get(3);
			diff = diaFinalTrimestre2.getTime() - diaIniciTrimestre2.getTime();
			numSetmanes = (diff / (24 * 60 * 60 * 1000)) / 7;

			calculoAmonest = null;
			calculoExpuls = null;

			fileWriter.append("2r Trimestre   Curs: " + dateCurs);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Headers

			// CONSULTA
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(COMMA_DELIMITER);

			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append("GRUP");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Nº ALUMNES");

			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("A");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("E");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("TOTAL");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Partes per alumne i grup");
			fileWriter.append(COMMA_DELIMITER);

			fileWriter.append(NEW_LINE_SEPARATOR);

			for (int i = 0; i < grupos.size(); i++) {
				fileWriter.append(grupos.get(i).getId());
				fileWriter.append(COMMA_DELIMITER);

				// FOR STUDENT ID
				query = new ReportQuerys();
				List ids = query.getIdAlumnes(grupos.get(i).getId());

				// query.closeTransaction();

				List idList = new ArrayList<>();

				for (int j = 0; j < ids.size(); j++) {
					idList.add(ids.get(j));

					// System.out.println(ids.get(j));
				}
				fileWriter.append(String.valueOf(idList.size()));
				fileWriter.append(COMMA_DELIMITER);

				total = 0;
				totalAmonest = 0;
				totalExpuls = 0;
				mediaParteAlumnoGrupo = 0;
				/////////////////////////////

				calculoExpuls = new ArrayList<>();
				calculoAmonest = new ArrayList<>();

				calculoAmonest = calcularAmonestadosPorSemana(idList, diaIniciTrimestre2, diaFinalTrimestre2);
				calculoExpuls = calcularExpulsadosPorSemana(idList, diaIniciTrimestre2, diaFinalTrimestre2);

				for (int n = 0; n < calculoAmonest.size(); n++) {
					totalAmonest = totalAmonest + Integer.parseInt(calculoAmonest.get(n).toString());
				}

				for (int n = 0; n < calculoExpuls.size(); n++) {
					totalExpuls = totalExpuls + Integer.parseInt(calculoExpuls.get(n).toString());
				}

				total = totalExpuls + totalAmonest;
				mediaParteAlumnoGrupo = ((total / 1024.0f) * 255) / ((idList.size() / 1024.0f) * 255);

				////////////////////////////////////
				if (totalAmonest == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(totalAmonest));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (totalExpuls == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(totalExpuls));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (total == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(total));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (mediaParteAlumnoGrupo == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(Float.toString(mediaParteAlumnoGrupo));

				}
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(NEW_LINE_SEPARATOR);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void calcularResumen2Trimestre3() {
		FileWriter fileWriter = null;

		Date diaIniciTrimestre3;
		Calendar diaIniciCal;
		List calculoAmonest;
		List calculoExpuls;
		Date diaFinalTrimestre3;
		long diff;
		long numSetmanes;
		int totalAmonest = 0;
		int totalExpuls = 0;
		int total = 0;
		float mediaParteAlumnoGrupo = 0;

		try {

			dates = readFile();
			File f = new File("/tmp/trimestre3");
			if (!f.exists()) {
				f.mkdirs();
			}
			fileWriter = new FileWriter("/tmp/trimestre3/resumen2.xls");
			query = new ReportQuerys();
			String dateCurs = query.getDateCurs();
			// query.closeTransaction();

			jpa = new GroupJPAManager();
			grupos = new ArrayList<>();
			grupos = jpa.getGroups();
			// jpa.closeTransaction();

			diaIniciTrimestre3 = dates.get(4);
			diaIniciCal = Calendar.getInstance();
			diaIniciCal.setTime(diaIniciTrimestre3);
			diaFinalTrimestre3 = dates.get(5);
			diff = diaFinalTrimestre3.getTime() - diaIniciTrimestre3.getTime();
			numSetmanes = (diff / (24 * 60 * 60 * 1000)) / 7;

			calculoAmonest = null;
			calculoExpuls = null;

			fileWriter.append("3r Trimestre   Curs: " + dateCurs);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Headers

			// CONSULTA
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(COMMA_DELIMITER);

			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append("GRUP");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Nº ALUMNES");

			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("A");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("E");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("TOTAL");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Partes per alumne i grup");
			fileWriter.append(COMMA_DELIMITER);

			fileWriter.append(NEW_LINE_SEPARATOR);

			for (int i = 0; i < grupos.size(); i++) {
				fileWriter.append(grupos.get(i).getId());
				fileWriter.append(COMMA_DELIMITER);

				// FOR STUDENT ID
				query = new ReportQuerys();
				List ids = query.getIdAlumnes(grupos.get(i).getId());

				// query.closeTransaction();

				List idList = new ArrayList<>();

				for (int j = 0; j < ids.size(); j++) {
					idList.add(ids.get(j));

					// System.out.println(ids.get(j));
				}
				fileWriter.append(String.valueOf(idList.size()));
				fileWriter.append(COMMA_DELIMITER);

				total = 0;
				totalAmonest = 0;
				totalExpuls = 0;
				mediaParteAlumnoGrupo = 0;
				/////////////////////////////

				calculoExpuls = new ArrayList<>();
				calculoAmonest = new ArrayList<>();

				calculoAmonest = calcularAmonestadosPorSemana(idList, diaIniciTrimestre3, diaFinalTrimestre3);
				calculoExpuls = calcularExpulsadosPorSemana(idList, diaIniciTrimestre3, diaFinalTrimestre3);

				for (int n = 0; n < calculoAmonest.size(); n++) {
					totalAmonest = totalAmonest + Integer.parseInt(calculoAmonest.get(n).toString());
				}

				for (int n = 0; n < calculoExpuls.size(); n++) {
					totalExpuls = totalExpuls + Integer.parseInt(calculoExpuls.get(n).toString());
				}

				total = totalExpuls + totalAmonest;
				mediaParteAlumnoGrupo = ((total / 1024.0f) * 255) / ((idList.size() / 1024.0f) * 255);

				////////////////////////////////////
				if (totalAmonest == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(totalAmonest));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (totalExpuls == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(totalExpuls));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (total == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(String.valueOf(total));

				}
				fileWriter.append(COMMA_DELIMITER);

				if (mediaParteAlumnoGrupo == 0) {
					fileWriter.append("");

				} else {
					fileWriter.append(Float.toString(mediaParteAlumnoGrupo));

				}
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(NEW_LINE_SEPARATOR);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private ArrayList<Date> readFile() throws ReadOnlyException, ConversionException, IOException {
		FileReader reader;
		String path2 = null;
		File currDir = new File(".");
		String linea = null;
		Date fechaIniciTrimestre1 = null;
		Date fechaFinalTrimestre3 = null;
		Date fechafinaltrimestre1 = null;
		Date fechafinaltrimestre2 = null;
		Date fechaIniciTrimestre3 = null;
		Date fechaIniciTrimestre2 = null;
		ArrayList<Date> fechas = new ArrayList<Date>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

		try {
			path2 = currDir.getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		File f = new File(rb.getString("file_settings"));
		BufferedReader br = new BufferedReader(new FileReader(f));
		if (br.readLine() == null) {
			System.out.println("No Hay fecha en el documento settings");

		} else {

			reader = new FileReader(f);
			BufferedReader flux = new BufferedReader(reader);

			while ((linea = flux.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(linea, ",");
				while (st.hasMoreTokens()) {

					try {

						fechaIniciTrimestre1 = formatter.parse(st.nextToken());
						fechafinaltrimestre1 = formatter.parse(st.nextToken());
						fechaIniciTrimestre2 = formatter.parse(st.nextToken());
						fechafinaltrimestre2 = formatter.parse(st.nextToken());
						fechaIniciTrimestre3 = formatter.parse(st.nextToken());
						fechaFinalTrimestre3 = formatter.parse(st.nextToken());

					} catch (ParseException e) {
						e.printStackTrace();
					}

					st.nextToken();
					st.nextToken();
					st.nextToken();

				}
				fechas.add(fechaIniciTrimestre1);
				fechas.add(fechafinaltrimestre1);
				fechas.add(fechaIniciTrimestre2);
				fechas.add(fechafinaltrimestre2);
				fechas.add(fechaIniciTrimestre3);
				fechas.add(fechaFinalTrimestre3);

			}

		}
		return fechas;
	}

	public void closeAllConnections() {
		query.closeTransaction();
		jpa.closeTransaction();
	}
}
