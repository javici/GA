package com.example.view.AdminView.Warning;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.declarative.Design;

/** 
 * !! DO NOT EDIT THIS FILE !!
 * 
 * This class is generated by Vaadin Designer and will be overwritten.
 * 
 * Please make a subclass with logic and additional interfaces as needed,
 * e.g class LoginView extends LoginDesign implements View { … }
 */
@DesignRoot
@AutoGenerated
@SuppressWarnings("serial")
public class AdminWarning extends CssLayout {
	public TextField nom;
	public TextField cognoms;
	public TextField grup;
	public PopupDateField datefield;
	public TextField time;
	public ComboBox comboSubject;
	public NativeSelect circunstancia;
	public ComboBox comboProf;
	public TextField tutor;
	public NativeSelect caracter;
	public OptionGroup accio;
	public OptionGroup motiu;
	public OptionGroup motiu2;
	public TextArea amotius;
	public Button bcancelar;
	public Button baceptar;

	public AdminWarning() {
		Design.read(this);
	}
}
