package org.openswing.swing.client;

import javax.swing.*;
import java.awt.*;
import org.openswing.swing.client.*;
import org.openswing.swing.util.client.*;
import org.openswing.swing.internationalization.java.*;
import org.openswing.swing.util.java.Consts;
import org.openswing.swing.properties.client.PropertyGridModel;
import org.openswing.swing.properties.client.PropertyCellEditor;
import org.openswing.swing.properties.client.PropertyCellRenderer;
import java.util.Hashtable;
import org.openswing.swing.logger.client.Logger;
import org.openswing.swing.message.receive.java.ValueObject;
import java.beans.Introspector;
import java.beans.*;
import org.openswing.swing.properties.client.PropertyGridController;
import java.lang.reflect.*;


/**
 * <p>Title: OpenSwing Framework</p>
 * <p>Description: Grid control having two columns: property name and property value,
 * where each row (each property) has its own data type (text, numeric, date, check-box, lookup, etc.), expressed as input control.
 * Hence this control can be used to set a collection of properties.
 * Note that PropertyGridControl is not an input control so it cannot be added to a Form panel.</p>
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
public class PropertyGridControl extends JScrollPane implements DataController {

  /** editable table model having a specific cell editor for each row */
  private PropertyGridModel model = new PropertyGridModel();

  /** table that contains properties */
  private JTable grid = new JTable(model);

  /** button linked to the grid, used to set INSERT mode (insert new row on grid) */
  private InsertButton insertButton = null;

  /** button linked to the grid, used to set EDIT mode (edit the selected row on grid) */
  private EditButton editButton = null;

  /** button linked to the grid, used to set READONLY mode and to reload the TableModel */
  private ReloadButton reloadButton = null;

  /** button linked to the grid, used to commit the modified rows (on INSERT/EDIT mode) */
  private SaveButton saveButton = null;

  /** controller used to fetch data (property values) */
  private PropertyGridController controller = new PropertyGridController();


  public PropertyGridControl() {
    try {
      jbInit();
      grid.getColumnModel().removeColumn(grid.getColumnModel().getColumn(2)); // remove input control column...
      grid.getColumnModel().removeColumn(grid.getColumnModel().getColumn(2)); // remove default value column...
      grid.getColumnModel().getColumn(1).setCellEditor(new PropertyCellEditor());
      grid.getColumnModel().getColumn(1).setCellRenderer(new PropertyCellRenderer());
      grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      grid.setAutoResizeMode(grid.AUTO_RESIZE_OFF);
      grid.setRowHeight(ClientSettings.CELL_HEIGHT);
      grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      grid.setSurrendersFocusOnKeystroke(true);
      grid.setBackground(ClientSettings.GRID_CELL_BACKGROUND);
      grid.setForeground(ClientSettings.GRID_CELL_FOREGROUND);
      grid.setSelectionBackground(ClientSettings.GRID_SELECTION_BACKGROUND);
      grid.setSelectionForeground(ClientSettings.GRID_SELECTION_FOREGROUND);

    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  private void jbInit() throws Exception {
    this.getViewport().add(grid, null);
  }


  /**
   * @return current grid mode
   */
  public final int getMode() {
    return model.getMode();
  }


  /**
   * Set grid mode.
   * @param mode grid mode; possibile values: READONLY, INSERT, EDIT
   */
  public final void setMode(int mode) {
    model.setMode(mode);
    grid.repaint();
  }


  /**
   * Clear grid content.
   */
  public final void clearData() {
    while(model.getRowCount()>0)
      model.removeRow(0);
    grid.revalidate();
    grid.repaint();
  }


  /**
   * Add a property to the grid.
   * @param propertyDescription description of the property; this is NOT automatically translated: use ClientSettings.getInstance().getResources().getResource() to translate it
   * @param inputControl type of property; this input control must have setted "attributeName" and "required" properties
   * @param defaultValue default value, used when the grid is in INSERT mode
   * @param userObject additional info (optional)
   * @return <code>false</code> if the specified attribute name has already been used for another row, <code>true</code> otherwise
   */
  public final boolean addProperty(String propertyDescription,InputControl inputControl,Object defaultValue,Object userObject) {
    model.addRow(new Object[]{propertyDescription,null,inputControl,defaultValue,userObject});
    return true;
  }


  /**
   * @param attributeName attribute name that identify a row
   * @return row index related to the specified attribute name
   */
  public final int findRow(String attributeName) {
    int rowIndex = model.findRow(attributeName);
    if (rowIndex==-1)
      Logger.error(this.getClass().getName(), "findRow", "The specified attribute '"+attributeName+"' does not exist.",null);
    return rowIndex;
  }


  /**
   *
   * @param attributeName attribute name that identify the row
   * @param value property value to set
   * @return <code>false</code> if the specified attribute name has not been found in the model, <code>true</code> otherwise
   */
  public final boolean setPropertyValue(String attributeName,Object value) {
    int rowIndex = findRow(attributeName);
    if (rowIndex==-1)
      return false;
    setPropertyValue(rowIndex,value);
    return true;
  }


  /**
   * @param rowIndex row index in the model
   * @param value property value to set
   */
  public final void setPropertyValue(int rowIndex,Object value) {
    model.setPropertyValue(rowIndex,value);
  }


  /**
   * @param attributeName attribute name that identify the row
   * @return property value related to the specified attribute name
   */
  public final Object getPropertyValue(String attributeName) {
    int rowIndex = findRow(attributeName);
    if (rowIndex==-1)
      return null;
    return getPropertyValue(rowIndex);
  }


  /**
   * @param rowIndex row index in the model
   * @return property value related to the specified attribute name
   */
  public final Object getPropertyValue(int rowIndex) {
    return model.getPropertyValue(rowIndex);
  }


  /**
   * @param attributeName attribute name that identify the row
   * @return property value related to the specified attribute name
   */
  public final Object getOldPropertyValue(String attributeName) {
    int rowIndex = findRow(attributeName);
    if (rowIndex==-1)
      return null;
    return getOldPropertyValue(rowIndex);
  }


  /**
   * @param rowIndex row index in the model
   * @return property value related to the specified attribute name
   */
  public final Object getOldPropertyValue(int rowIndex) {
    return model.getOldPropertyValue(rowIndex);
  }


  /**
   * @param attributeName attribute name that identify the row
   * @return user object related to the specified attribute name
   */
  public final Object getUserObject(String attributeName) {
    int rowIndex = findRow(attributeName);
    if (rowIndex==-1)
      return null;
    return getUserObject(rowIndex);
  }


  /**
   * @param rowIndex row index in the model
   * @return user object related to the specified row index
   */
  public final Object getUserObject(int rowIndex) {
    return model.getUserObject(rowIndex);
  }


  /**
   * @param attributeName attribute name that identify the row
   * @return input control related to the specified row index
   */
  public final InputControl getInputControl(String attributeName) {
    int rowIndex = findRow(attributeName);
    if (rowIndex==-1)
      return null;
    return getInputControl(rowIndex);
  }


  /**
   * @param rowIndex row index in the model
   * @return input control related to the specified row index
   */
  public final InputControl getInputControl(int rowIndex) {
    return model.getInputControl(rowIndex);
  }


  /**
   * Add a set properties to the grid, retrieving them from the specified value object:
   * for each attribute defined in the v.o. will be created a row (a property) in the grid.
   * Also attribute values are retrieved from the v.o. and set in the grid as property values.
   * All properties are defined as mandatory.
   * @param valueObject value object used to create a set of properties, whose description is determined by translating attribute names
   */
  public final void addProperties(ValueObject valueObject) {
    try {
      PropertyDescriptor[] props =  Introspector.getBeanInfo(valueObject.getClass()).getPropertyDescriptors();
      InputControl inputControl = null;
      for(int i=0;i<props.length;i++) {
        inputControl = null;

        if (props[i].getPropertyType().equals(java.util.Date.class) ||
            props[i].getPropertyType().equals(java.sql.Date.class) ||
            props[i].getPropertyType().equals(java.sql.Timestamp.class))
          inputControl = new DateControl();
        else if (props[i].getPropertyType().equals(Integer.class) ||
                 props[i].getPropertyType().equals(Long.class)) {
          inputControl = new NumericControl();
          ((NumericControl)inputControl).setDecimals(0);
        }
        else if (props[i].getPropertyType().equals(Float.class) ||
                 props[i].getPropertyType().equals(Double.class) ||
                 props[i].getPropertyType().equals(java.math.BigDecimal.class)) {
          inputControl = new NumericControl();
          ((NumericControl)inputControl).setDecimals(5);
        }
        else if (props[i].getPropertyType().equals(byte[].class))
          inputControl = new ImageControl();
        else if (props[i].getPropertyType().equals(String.class))
          inputControl = new TextControl();

        if (inputControl!=null) {
          inputControl.setAttributeName(props[i].getName());
          model.addRow(new Object[]{
            ClientSettings.getInstance().getResources().getResource(props[i].getName()),
            props[i].getReadMethod().invoke(valueObject,new Object[0]),
            inputControl,
            props[i].getReadMethod().invoke(valueObject,new Object[0]),
            null
          });
        }
      }

    }
    catch (Exception ex) {
      Logger.error(this.getClass().getName(), "addProperties", ex.getMessage(), ex);
    }
  }


  /**
   * Set property values, based on the specified value object:
   * for each attribute defined in the v.o. that matches with a row in the grid, its value will be retrieved and set in the grid.
   */
  public final void setProperties(ValueObject valueObject) {
    try {
      PropertyDescriptor[] props =  Introspector.getBeanInfo(valueObject.getClass()).getPropertyDescriptors();
      int rowIndex = -1;
      for(int i=0;i<props.length;i++) {
        rowIndex = model.findRow(props[i].getName());
        if (rowIndex!=-1)
          setPropertyValue(rowIndex,props[i].getReadMethod().invoke(valueObject,new Object[0]));
      }
    }
    catch (Exception ex) {
      Logger.error(this.getClass().getName(), "setProperties", ex.getMessage(), ex);
    }
  }


  /**
   * Set value object content, according to property values:
   * for each attribute defined in the v.o. that matches with a row in the grid, its value will be retrieved from the grid and set into the v.o.
   */
  public final void getProperties(ValueObject valueObject) {
    try {
      PropertyDescriptor[] props =  Introspector.getBeanInfo(valueObject.getClass()).getPropertyDescriptors();
      int rowIndex = -1;
      Object obj = null;
      for(int i=0;i<props.length;i++) {
        rowIndex = model.findRow(props[i].getName());
        if (rowIndex != -1) {
          obj = getPropertyValue(rowIndex);
          try {
            if (obj!=null && !obj.getClass().equals(props[i].getPropertyType())) {
              // convert property value, according to property type...
              if (java.util.Date.class.isAssignableFrom(obj.getClass())) {
                if (props[i].getPropertyType().equals(java.sql.Date.class))
                  obj = new java.sql.Date(((java.util.Date)obj).getTime());
                else if (props[i].getPropertyType().equals(java.sql.Timestamp.class))
                  obj = new java.sql.Timestamp(((java.util.Date)obj).getTime());
              }
              else if (Number.class.isAssignableFrom(obj.getClass())) {
                if (props[i].getPropertyType().equals(Integer.class))
                  obj = new Integer(((Number)obj).intValue());
                else if (props[i].getPropertyType().equals(Long.class))
                  obj = new Long(((Number)obj).longValue());
                else if (props[i].getPropertyType().equals(Float.class))
                  obj = new Float(((Number)obj).floatValue());
                else if (props[i].getPropertyType().equals(Double.class))
                  obj = new Double(((Number)obj).doubleValue());
                else if (props[i].getPropertyType().equals(java.math.BigDecimal.class))
                  obj = new java.math.BigDecimal(((Number)obj).doubleValue());
              }

            }
            props[i].getWriteMethod().invoke(valueObject,new Object[] {obj});
          }
          catch (IllegalArgumentException ex1) {
          }
        }

      }
    }
    catch (Exception ex) {
      Logger.error(this.getClass().getName(), "getProperties", ex.getMessage(), ex);
    }
  }


  /**
   * @param rowIndex row index
   * @return attribute name, related to the specified row index
   */
  public final String getAttributeName(int rowIndex) {
    return model.getAttributeName(rowIndex);
  }





  /**
   * @return insert button linked to grid
   */
  public InsertButton getInsertButton() {
    return insertButton;
  }


  /**
   * @return edit button linked to grid
   */
  public EditButton getEditButton() {
    return editButton;
  }


  /**
   * Set edit button linked to grid.
   * @param editButton edit button linked to grid
   */
  public void setEditButton(EditButton editButton) {
    if (this.editButton != null)
      this.editButton.removeDataController(this);
    this.editButton = editButton;
    if (editButton != null)
      editButton.addDataController(this);
  }


  /**
   * Set insert button linked to grid.
   * @param insertButton insert button linked to grid
   */
  public void setInsertButton(InsertButton insertButton) {
    if (this.insertButton != null)
      this.insertButton.removeDataController(this);
    this.insertButton = insertButton;
    if (insertButton != null)
      insertButton.addDataController(this);
  }


  /**
   * @return reload button linked to grid
   */
  public ReloadButton getReloadButton() {
    return reloadButton;
  }


  /**
   * Set reload button linked to grid.
   * @param reloadButton reload button linked to grid
   */
  public void setReloadButton(ReloadButton reloadButton) {
    if (this.reloadButton != null)
      this.reloadButton.removeDataController(this);
    this.reloadButton = reloadButton;
    if (reloadButton != null)
      reloadButton.addDataController(this);
  }


  /**
   * @return save button linked to grid.
   */
  public SaveButton getSaveButton() {
    return saveButton;
  }


  /**
   * Set save button linked to grid.
   * @param saveButton save button linked to grid
   */
  public void setSaveButton(SaveButton saveButton) {
    if (this.saveButton != null)
      this.saveButton.removeDataController(this);
    this.saveButton = saveButton;
    if (saveButton != null) {
      //imposta listener per nuovo pulsante
      saveButton.addDataController(this);
      saveButton.setEnabled(false);
    }
  }


  /**
   * reload
   */
  public void reload() {

    int row = grid.getEditingRow();
    int col = grid.getEditingColumn();
    if (row>=0 && col>=0)
      grid.getCellEditor(row, col).stopCellEditing();

    if (getMode()!=Consts.READONLY) {
      // view confirmation dialog...
      if (JOptionPane.showConfirmDialog(ClientUtils.getParentFrame(this),
                                    ClientSettings.getInstance().getResources().getResource("Cancel changes and reload data?"),
                                    ClientSettings.getInstance().getResources().getResource("Attention"),
                                    JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
        setMode(Consts.READONLY);
        controller.loadData(this);
        grid.repaint();
        if (getInsertButton()!=null)
          getInsertButton().setEnabled(true);
        if (getEditButton()!=null)
          getEditButton().setEnabled(true);
        if (getReloadButton()!=null)
          getReloadButton().setEnabled(true);
        if (getSaveButton()!=null)
          getSaveButton().setEnabled(false);
        grid.setRowSelectionInterval(0,0);
        grid.setColumnSelectionInterval(0,0);
      }
    } else if (getMode()==Consts.READONLY) {
      setMode(Consts.READONLY);
      controller.loadData(this);
      grid.repaint();
      if (getInsertButton()!=null)
        getInsertButton().setEnabled(true);
      if (getEditButton()!=null)
        getEditButton().setEnabled(true);
      if (getReloadButton()!=null)
        getReloadButton().setEnabled(true);
      if (getSaveButton()!=null)
        getSaveButton().setEnabled(false);
      grid.setRowSelectionInterval(0,0);
      grid.setColumnSelectionInterval(0,0);
    }
  }

  /**
   * insert
   */
  public void insert() {
    if (getMode()==Consts.READONLY) {
      setMode(Consts.INSERT);
      if (getInsertButton()!=null)
        getInsertButton().setEnabled(false);
      if (getEditButton()!=null)
        getEditButton().setEnabled(false);
      if (getReloadButton()!=null)
        getReloadButton().setEnabled(true);
      if (getSaveButton()!=null)
        getSaveButton().setEnabled(true);
      grid.setRowSelectionInterval(0,0);
      grid.setColumnSelectionInterval(0,0);
      grid.editCellAt(grid.getSelectedRow(),grid.getSelectedColumn());
    }
    else
      Logger.error(this.getClass().getName(),"insert","Setting grid to insert mode is not allowed: grid must be in read only mode.",null);
  }

  /**
   * copy
   */
  public void copy() {
  }

  /**
   * edit
   */
  public void edit() {
    if (getMode()==Consts.READONLY) {
      setMode(Consts.EDIT);
      if (getInsertButton()!=null)
        getInsertButton().setEnabled(false);
      if (getEditButton()!=null)
        getEditButton().setEnabled(false);
      if (getReloadButton()!=null)
        getReloadButton().setEnabled(true);
      if (getSaveButton()!=null)
        getSaveButton().setEnabled(true);
      if (grid.getSelectedRow()==-1) {
        grid.setRowSelectionInterval(0, 0);
      }
      grid.setColumnSelectionInterval(0,0);
      grid.requestFocus();

      int col = 0;
      grid.editCellAt(grid.getSelectedRow(),grid.getSelectedColumn());

    }
    else
      Logger.error(this.getClass().getName(),"edit","Setting grid to edit mode is not allowed: grid must be in read only mode.",null);
  }

  /**
   * delete
   */
  public void delete() {
  }


  /**
   * Execute a validation on changed rows.
   * @return <code>true</code> if all changed rows are in a valid state, <code>false</code> otherwise
   */
  public final boolean validateRows() {
    try {
      if (getMode()!=Consts.READONLY) {
        // validate pending changes...
        int row = grid.getEditingRow();
        int col = grid.getEditingColumn();
        if ((row>=0) && (col>=0) && !grid.getCellEditor(row, col).stopCellEditing())
          // pending invalid changes found...
          return false;

        int[] rows = new int[0];
        if (getMode() == Consts.EDIT)
          rows = model.getChangedRowNumbers();
        // all changed rows are validated...
        for (int i=0; i<rows.length; i++) {
          if (model.isRequired(i) && model.getValueAt(i,1)==null) {
            JOptionPane.showMessageDialog(ClientUtils.getParentFrame(this),
                                          ClientSettings.getInstance().getResources().getResource("A mandatory column is empty."),
                                          ClientSettings.getInstance().getResources().getResource("Value not valid"),
                                          JOptionPane.ERROR_MESSAGE);
            grid.editCellAt(i, 1);
            grid.requestFocus();
            return false;
          }
        }
        return(true);
      }
      else
        // validation skipped because grid is in read only mode...
        return true;
    }
    catch (Throwable ex) {
      Logger.error(this.getClass().getName(), "validateRows", "Error on validating columns.", ex);
      return(false);
    }
  }


  /**
   * Method called when user clicks on save button.
   * @return <code>true</code> if saving operation was correctly completed, <code>false</code> otherwise
   */
  public final boolean save() {
    int previousMode; // current grid mode...
    boolean response = false;
    if (getMode()!=Consts.READONLY) {
      try {
        // all columns are validated: if a validation error occours then saving is interrupted...
        if (!validateRows())
          return false;
      }
      catch (Exception ex) {
        Logger.error(this.getClass().getName(), "save", "Error on grid validation.", ex);
      }
      try {
        // call grid controller to save data...
        previousMode = getMode();
        if (getMode()==Consts.INSERT) {
          response = controller.insertRecords(this,model.getChangedRowNumbers());
        }
        else if (getMode()==Consts.EDIT) {
          response = controller.updateRecords(this,model.getChangedRowNumbers());
        }
        if (response) {
          try {
            // patch inserted to disable image cell editor...
            int row = grid.getEditingRow();
            int col = grid.getEditingColumn();
          }
          catch (Exception ex1) {
          }

          model.setMode(Consts.READONLY);
          if (getReloadButton()!=null)
            getReloadButton().setEnabled(true);
          if (getSaveButton()!=null)
            getSaveButton().setEnabled(false);
          // reset toolbar buttons state...
          if (getEditButton()!=null)
            getEditButton().setEnabled(true);
          if (getInsertButton()!=null)
            getInsertButton().setEnabled(getInsertButton().getOldValue());

        } else {
          // saving operation throws an error: it will be viewed on a dialog...
          JOptionPane.showMessageDialog(
              ClientUtils.getParentFrame(this),
              ClientSettings.getInstance().getResources().getResource("Error while saving"),
              ClientSettings.getInstance().getResources().getResource("Saving Error"),
              JOptionPane.ERROR_MESSAGE
          );
        }
      }
      catch (Exception ex) {
        Logger.error(this.getClass().getName(), "save", "Error while saving.", ex);
        JOptionPane.showMessageDialog(
            ClientUtils.getParentFrame(this),
            ClientSettings.getInstance().getResources().getResource("Error while saving")+":\n"+ex.getMessage(),
            ClientSettings.getInstance().getResources().getResource("Saving Error"),
            JOptionPane.ERROR_MESSAGE
        );
      }
    }
    else
      Logger.error(this.getClass().getName(),"save","Saving data is not allowed in read only mode.",null);
    return response;
  }


  /**
   * export
   */
  public void export() {
  }

  /**
   * getFunctionId
   *
   * @return String
   */
  public String getFunctionId() {
    return "";
  }

  /**
   * isButtonDisabled
   *
   * @param button GenericButton
   * @return boolean
   */
  public boolean isButtonDisabled(GenericButton button) {
    return false;
  }

  /**
   * filterSort
   */
  public void filterSort() {
  }


  /**
   * @return controller used to fetch data (property values)
   */
  public final PropertyGridController getController() {
    return controller;
  }


  /**
   * Set the controller used to fetch data (property values).
   * @param controller controller used to fetch data (property values)
   */
  public final void setController(PropertyGridController controller) {
    this.controller = controller;
  }


  public final void setPropertyNameWidth(int width) {
    grid.getColumnModel().getColumn(0).setPreferredWidth(width);
  }


  public final void setPropertyValueWidth(int width) {
    grid.getColumnModel().getColumn(1).setPreferredWidth(width);
  }


  public final int getPropertyNameWidth(int width) {
    return grid.getColumnModel().getColumn(0).getPreferredWidth();
  }


  public final int getPropertyValueWidth() {
    return grid.getColumnModel().getColumn(1).getPreferredWidth();
  }


}