package org.openswing.swing.table.editors.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import org.openswing.swing.client.*;
import org.openswing.swing.util.client.*;
import java.util.ArrayList;
import org.openswing.swing.client.SpinnerListControl;
import org.openswing.swing.domains.java.*;
import javax.swing.event.ChangeListener;
import org.openswing.swing.logger.client.Logger;
import org.openswing.swing.table.client.Grids;


/**
 * <p>Title: OpenSwing Framework</p>
 * <p>Description: Column editor used for spinner list type columns.</p>
 * <p>Copyright: Copyright (C) 2006 Mauro Carniel</p>
 *
 * <p> This file is part of OpenSwing Framework.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the (LGPL) Lesser General Public
 * License as published by the Free Software Foundation;
 *
 *                GNU LESSER GENERAL PUBLIC LICENSE
 *                 Version 2.1, February 1999
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *       The author may be contacted at:
 *           maurocarniel@tin.it</p>
 *
 * @author Mauro Carniel
 * @version 1.0
 */
public class SpinnerListCellEditor extends AbstractCellEditor implements TableCellEditor {

  /** domain linked to the combo-box */
  private Domain domain = null;

  /** list of couples (code, description) */
  private DomainPair[] pairs = null;

  SpinnerListModel model = new SpinnerListModel();

  /** text input field */
  JSpinner field = new JSpinner(model) {

    private KeyEvent oldEv = null;

      public boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                          int condition, boolean pressed) {
        if (e.getSource()!=null && e.getSource() instanceof org.openswing.swing.table.client.Grid) {
          try {
            if (oldEv==null || !e.equals(oldEv)) {
              oldEv = e;
//              field.processKeyEvent(e);
              oldEv = null;
            }
          }
          catch (Exception ex) {
          }
        }
        else if (e.getKeyChar()=='\t' || e.getKeyChar()=='\n')
          stopCellEditing();
         return true;
      }

  };

  JFormattedTextField ftf = getTextField(field);

  /** flag used to set mandatory property of the cell */
  private boolean required;

  /** table */
  private JTable table = null;

  /** current selected row */
  private int row = -1;

  /** current selected column */
  private int col = -1;

  private int textOrientation;

  private ArrayList changeListeners = null;

  /** define if description in combo items must be translated */
  private boolean translateItemDescriptions;

  /** table hook */
  private Grids grids = null;


  public SpinnerListCellEditor(Grids grids,
                               boolean required,
                               int textOrientation,
                               ArrayList changeListeners,
                               boolean translateItemDescriptions,
                               Domain domain,
                               ComponentOrientation orientation) {
    this.grids = grids;
    this.domain = domain;
    this.translateItemDescriptions = translateItemDescriptions;
    this.required = required;
    this.textOrientation = textOrientation;
    this.changeListeners = changeListeners;

    for (int i = 0; i < changeListeners.size(); i++)
      field.addChangeListener( ( (ChangeListener) changeListeners.get(i)));

    if (orientation != null)
      field.setComponentOrientation(orientation);

    field.addKeyListener(new KeyAdapter() {

      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == e.VK_TAB) {
          stopCellEditing();
          table.requestFocus();
          try {
            table.setColumnSelectionInterval(col + 1, col + 1);
          }
          catch (Exception ex) {
          }
        }
      }

    });
    setDomain(domain);
  }


  public void setDomain(Domain domain) {
    if (domain!=null) {
      ArrayList list = new ArrayList();
      DomainPair[] pairs = domain.getDomainPairList();
      for(int i=0;i<pairs.length;i++)
        if (translateItemDescriptions)
          list.add(ClientSettings.getInstance().getResources().getResource(pairs[i].getDescription()));
        else
          list.add(pairs[i].getDescription());
      model.setList(list);
      field.setModel(model);
    }

  }


  /**
   * Return the formatted text field used by the editor, or
   * null if the editor doesn't descend from JSpinner.DefaultEditor.
   */
  public JFormattedTextField getTextField(JSpinner spinner) {
      JComponent editor = spinner.getEditor();
      if (editor instanceof JSpinner.DefaultEditor) {
          return ((JSpinner.DefaultEditor)editor).getTextField();
      } else {
        Logger.error(this.getClass().getName(),"getTextField","Unexpected editor type: "+spinner.getEditor().getClass()+" isn't a descendant of DefaultEditor",null);
          return null;
      }
  }


  /**
   * Stop cell editing. This method stops cell editing (effectively committing the edit) only if the data entered is validated successfully.
   * @return <code>true</code> if cell editing may stop, and <code>false</code> otherwise.
   */
  public final boolean stopCellEditing() {
    return validate();
  }


  /**
   * Perform the validation.
   */
  private boolean validate() {
    boolean ok = true;;
    fireEditingStopped();
    return ok;
  }


  public Object getCellEditorValue() {
    return getValue();
  }


  /**
   * Select the spinner item related to the specified code.
   * @param code used to retrieve the corresponding item and to select that item in the spinner
   */
  public final void setValue(Object code) {
    if (code==null)
      field.setValue(null);
    if (domain==null)
      return;
    DomainPair pair = domain.getDomainPair(code);
    if (pair!=null) {
      if (translateItemDescriptions)
        field.setValue( ClientSettings.getInstance().getResources().getResource(pair.getDescription()) );
      else
        field.setValue( pair.getDescription() );
    }
  }


  /**
   * @return code related to the selected spinner item; it return null if no item is selected
   */
  public final Object getValue() {
    if (domain==null)
      return null;
    DomainPair[] pairs = domain.getDomainPairList();
    for(int i=0;i<pairs.length;i++)
      if (field.getValue()==null)
        return null;
      else {
        if (translateItemDescriptions) {
          if (field.getValue().equals( ClientSettings.getInstance().getResources().getResource(pairs[i].getDescription()) ))
            return pairs[i].getCode();
        }
        else {
          if (field.getValue().equals( pairs[i].getDescription()) )
            return pairs[i].getCode();
        }
      }
    return null;
  }


  public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected, int row,
                                               int column) {
    if (defaultFont==null)
      defaultFont = field.getFont();

    this.table = table;
    this.row = row;
    this.col = column;

    setValue(value);

    if (required) {
      field.setBorder(BorderFactory.createLineBorder(ClientSettings.GRID_REQUIRED_CELL_BORDER));
//      field.setBorder(new CompoundBorder(new RequiredBorder(),field.getBorder()));
    }

    java.awt.Font f = grids.getGridController().getFont(row,table.getModel().getColumnName(table.convertColumnIndexToModel(column)),value,defaultFont);
    if (f != null)
      field.setFont(f);
    else
      field.setFont(defaultFont);

    return field;
  }


  public final void finalize() {
    table = null;
    field = null;
  }



}


