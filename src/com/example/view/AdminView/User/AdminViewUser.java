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
package com.example.view.AdminView.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.example.Entities.Student;
import com.example.Entities.Teacher;
import com.example.Entities.User;
import com.example.Logic.EntityManagerUtil;
import com.example.Logic.StudentsJPAManager;
import com.example.Logic.TeachersJPAManager;
import com.example.Logic.UserJPAManager;
import com.example.LoginView.LoginView;
import com.example.Templates.MainContentView;
import com.example.view.AdminView.AdminView;
import com.example.view.AdminView.CSV.AdminCSVUpload;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.themes.ValoTheme;

public class AdminViewUser extends MainContentView {
	private Grid grid;
	private Window windowEdit = new Window();
	private AdminViewEditUser userformEdit;
	private JPAContainer<User> usuaris;
	private UserJPAManager MA;
	private EntityManagerUtil entman ;
	private EntityManager em ;

	public AdminViewUser() {

		entman = new EntityManagerUtil();
		em = entman.getEntityManager();
		
		userformEdit = new AdminViewEditUser();

		buttonsSettings();
		filterTextProperties();
		WindowProperties();
		buttonsAction();
		vHorizontalMain.removeAllComponents();
		vHorizontalMain.addComponent(GridProperties());

	}

	private void buttonsAction() {
		// TODO Auto-generated method stub
		
		userformEdit.aceptarButton.setClickShortcut(KeyCode.ENTER);
		userformEdit.cancelarButton.setClickShortcut(KeyCode.ENTER);
		
		bAdd.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				getItemSelected();

			}
		});

		buttonEdit.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				// getItemSelectedToTeacherForm();
				editUser();
			}

		});
		bDelete.addClickListener(e -> deleteUser());
	}

	private void editUser() {


		getItemSelected();

		Object id = grid.getContainerDataSource().getItem(grid.getSelectedRow()).getItemProperty("id");
		Object user = grid.getContainerDataSource().getItem(grid.getSelectedRow()).getItemProperty("username");


		userformEdit.txtGrup.setValue(user.toString());
		userformEdit.txtGrup.setReadOnly(true);

		userformEdit.aceptarButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				
				MA = new UserJPAManager();
				User user = getUserEdit();
				MessageDigest md = null;
				try {
					md = MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HexBinaryAdapter hbinary = new HexBinaryAdapter();
				String password = userformEdit.txtPassword.getValue();
				
				
				String passwordhash = hbinary.marshal(md.digest(password.getBytes())).toLowerCase();

				MA.updateUser(new User(user.getId(), passwordhash));
				MA.closeTransaction();
				reloadGrid();
				windowEdit.close();
				clearEditForm();	
				getUI().getNavigator().navigateTo(AdminView.NAME);

				notif("Usuari editat correctament");
				
				}

		});

		userformEdit.cancelarButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				windowEdit.close();
			}
		});
	}

	private void clearEditForm() {
		// TODO Auto-generated method stub

		userformEdit.txtPassword.clear();

	}

	private User getUserEdit() {

		Object id = grid.getContainerDataSource().getItem(grid.getSelectedRow()).getItemProperty("id");
		String pass = userformEdit.txtPassword.getValue().toString();
		
		
		User usuari = new User(Integer.parseInt(id.toString()), pass);

		return usuari;
	}

	private void getItemSelected() {

		UI.getCurrent().addWindow(windowEdit);

	}

	private void WindowProperties() {
		
		windowEdit.setWidth(900.0f, Unit.PIXELS);
		windowEdit.setHeight("45%");
		windowEdit.setWidth("45%");
		windowEdit.setDraggable(false);
		windowEdit.setModal(true);
		windowEdit.setCaption("Editar usuari");
		windowEdit.center();
		windowEdit.setContent(userformEdit);
	}

	public Grid GridProperties() {

		// Fill the grid with data
		usuaris = JPAContainerFactory.make(User.class, em);
		grid = new Grid("", usuaris);
		grid.setSizeFull();
		grid.setContainerDataSource(usuaris);
		grid.setColumnReorderingAllowed(true);
		grid.setColumns("username", "rol");

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.addSelectionListener(new SelectionListener() {

			@Override
			public void select(SelectionEvent event) {
				// TODO Auto-generated method stub
				bAdd.setEnabled(true);
				buttonEdit.setEnabled(true);
				bDelete.setEnabled(true);

			}
		});

		return grid;
	}

	private void deleteUser() {
		MA = new UserJPAManager();

		int id = Integer.parseInt(
				grid.getContainerDataSource().getItem(grid.getSelectedRow()).getItemProperty("id").toString());
		String usuari = grid.getContainerDataSource().getItem(grid.getSelectedRow()).getItemProperty("username")
				.toString();

		getUI().getCurrent().addWindow(DeleteSubWindows(id, usuari));

	}

	public Window DeleteSubWindows(int id, String nom) {

		Window win = new Window("Esborrar usuari");

		if (nom.equals("admin")) {

			win.setVisible(false);
			notif("No es pot borrar aquest usuari");

		} else {

			win.setWidth("20%");
			win.setHeight("20%");
			win.setIcon(FontAwesome.CLOSE);
			win.setDraggable(false);
			win.setResizable(false);
			win.setModal(true);
			win.center();

			Label question = new Label("Esborrar l'usuari " + nom);
			Button yes = new Button("Sí");
			Button no = new Button("No");
			yes.addStyleName(ValoTheme.BUTTON_FRIENDLY);
			no.addStyleName(ValoTheme.BUTTON_DANGER);
			no.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Auto-generated method stub
					win.close();

				}
			});

			yes.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Auto-generated method stub
					MA.removeUser(new User(id));
					MA.closeTransaction();
					win.close();
					notif("Usuari esborrat correctament");
					reloadGrid();
				}
			});

			HorizontalLayout buttons = new HorizontalLayout(yes, no);
			buttons.setSpacing(true);

			VerticalLayout content = new VerticalLayout(question, buttons);
			win.setContent(content);

		}

		return win;

	}

	public void notif(String mensaje) {

		Notification notif = new Notification(mensaje, null, Notification.Type.ASSISTIVE_NOTIFICATION, true); // Contains
																												// HTML

		// Customize it
		notif.show(Page.getCurrent());
		notif.setDelayMsec(500);
		notif.setPosition(Position.TOP_CENTER);
	}

	public void reloadGrid() {

		vHorizontalMain.removeAllComponents();
		vHorizontalMain.addComponent(GridProperties());

	}

	private TextField filterTextProperties() {
		// TODO Auto-generated method stub
		txtSearch.setInputPrompt("Filtra per nom");
		txtSearch.addTextChangeListener(new TextChangeListener() {

			SimpleStringFilter filter = null;

			@Override
			public void textChange(TextChangeEvent event) {
				// TODO Auto-generated method stub

				Filterable f = (Filterable) grid.getContainerDataSource();

				// Remove old filter
				if (filter != null)
					f.removeContainerFilter(filter);

				// Set new filter for the "Name" column
				filter = new SimpleStringFilter("username", event.getText(), true, false);
				f.addContainerFilter(filter);
			}
		});
		return txtSearch;
	}

	private void buttonsSettings() {
		// TODO Auto-generated method stub

		horizontalTitle.addStyleName("horizontal-title");
		txtTitle.addStyleName("main-title");
		clearTxt.setIcon(FontAwesome.TIMES);

		txtTitle.setValue("Gestió d'Usuaris");
		bDelete.addStyleName(ValoTheme.BUTTON_DANGER);
		bAdd.addStyleName(ValoTheme.BUTTON_PRIMARY);
		bRegister.addStyleName(ValoTheme.BUTTON_PRIMARY);
		buttonEdit.addStyleName(ValoTheme.BUTTON_PRIMARY);

		bDelete.setEnabled(false);
		buttonEdit.setEnabled(false);
		bRegister.setVisible(false);
		bAdd.setEnabled(false);
		bAdd.setVisible(false);
		clearTxt.setVisible(false);

		bAdd.setCaption("Donar d'alta");
		bDelete.setCaption("Donar de baixa");

	}

	public void clear() {
		// bAdd.setEnabled(false);
		bDelete.setEnabled(false);
		buttonEdit.setEnabled(false);
		grid.deselectAll();
	}

}
