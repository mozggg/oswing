package org.openswing.swing.table.client;

import java.awt.Color;
import org.openswing.swing.message.receive.java.*;
import org.openswing.swing.message.send.java.*;
import java.util.*;
import org.openswing.swing.client.*;

import org.openswing.swing.util.client.*;
import org.openswing.swing.util.java.Consts;
import org.openswing.swing.table.model.client.VOListAdapter;
import org.openswing.swing.table.model.client.VOListTableModel;
import java.awt.Font;


/**
 * <p>Title: OpenSwing Framework</p>
 * <p>Description: Grid Controller.</p>
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
public class GridController {

  /** delta row, i.e. number of grid rows to color; default value: 0 (no delta) */
  private int deltaRow = 0;

  /** background color to use when deltaRow>0 */
  private Color deltaColor = new Color(235,235,235);


  public GridController() {
    setDeltaRow(2);
  }


  /**
   * Callback method invoked when the user has pressed ENTER button on the selected row of the grid.
   * Default behaviour: it calls doubleClick method.
   * @param rowNumber selected row index
   * @param persistentObject v.o. related to the selected row
   */
  public void enterButton(int rowNumber,ValueObject persistentObject) {
    doubleClick(rowNumber,persistentObject);
  }


  /**
   * Callback method invoked when the user has double clicked on the selected row of the grid.
   * @param rowNumber selected row index
   * @param persistentObject v.o. related to the selected row
   */
  public void doubleClick(int rowNumber,ValueObject persistentObject) {
  }


  /**
   * Callback method invoked when a grid cell is selected.
   * @param rowNumber selected row index
   * @param columnIndex column index related to the column currently selected
   * @param attributedName attribute name related to the column currently selected
   * @param persistentObject v.o. related to the selected row
   */
  public void selectedCell(int rowNumber, int columnIndex, String attributedName, ValueObject persistentObject) { }



  /**
   * Method used to define the background color for each cell of the grid.
   * @param rowNumber selected row index
   * @param attributeName attribute name related to the column currently selected
   * @param value object contained in the selected cell
   * @return background color of the selected cell
   */
  public Color getBackgroundColor(int row,String attributeName,Object value) {
    if (deltaRow>0 && deltaColor!=null && row%deltaRow==0)
      return deltaColor;
    return ClientSettings.GRID_CELL_BACKGROUND;
  }


  /**
   * Method used to define the foreground color for each cell of the grid.
   * @param rowNumber selected row index
   * @param attributeName attribute name related to the column currently selected
   * @param value object contained in the selected cell
   * @return foreground color of the selected cell
   */
  public Color getForegroundColor(int row,String attributeName,Object value) {
    return ClientSettings.GRID_CELL_FOREGROUND;
  }


  /**
   * Callback method invoked each time a cell is edited: this method define if the new value if valid.
   * This method is invoked ONLY if:
   * - the edited value is not equals to the old value OR it has exmplicitely called setCellAt or setValueAt
   * - the cell is editable
   * Default behaviour: cell value is valid.
   * @param rowNumber selected row index
   * @param attributeName attribute name related to the column currently selected
   * @param oldValue old cell value (before cell editing)
   * @param newValue new cell value (just edited)
   * @return <code>true</code> if cell value is valid, <code>false</code> otherwise
   */
  public boolean validateCell(int rowNumber,String attributeName,Object oldValue,Object newValue) {
    return true;
  }


  /**
   * @return
   */
  public int getDeltaRow() {
    return deltaRow;
  }


  /**
   * Consente di definire la proprieta' che consente di definire un delta di righe colorate; valore di default: 0 (nessun delta).
   * @param deltaRow proprieta' che consente di definire un delta di righe colorate
   */
  public void setDeltaRow(int deltaRow) {
    this.deltaRow = deltaRow;
  }


  /**
   * @return background cell color used when deltaRow>0
   */
  public Color getDeltaColor() {
    return deltaColor;
  }


  /**
   * Set the background cell color used when deltaRow>0.
   * @param deltaColor background cell color
   */
  public void setDeltaColor(Color deltaColor) {
    this.deltaColor = deltaColor;
  }


  /**
   * Method invoked when the user has clicked on save button and the grid is in INSERT mode.
   * @param rowNumbers row indexes related to the new rows to save
   * @param newValueObjects list of new value objects to save
   * @return an ErrorResponse value object in case of errors, VOListResponse if the operation is successfully completed
   */
  public Response insertRecords(int[] rowNumbers, ArrayList newValueObjects) throws Exception {
    throw new java.lang.UnsupportedOperationException("Method insertRecords() not yet implemented.");
  }

  /**
   * Method invoked when the user has clicked on save button and the grid is in EDIT mode.
   * @param rowNumbers row indexes related to the changed rows
   * @param oldPersistentObjects old value objects, previous the changes
   * @param persistentObjects value objects relatied to the changed rows
   * @return an ErrorResponse value object in case of errors, VOListResponse if the operation is successfully completed
   */
  public Response updateRecords(int[] rowNumbers,ArrayList oldPersistentObjects,ArrayList persistentObjects) throws Exception {
    throw new java.lang.UnsupportedOperationException("Method updateRecords() not yet implemented.");
  }

  /**
   * Method invoked when the user has clicked on delete button and the grid is in READONLY mode.
   * @param persistentObjects value objects to delete (related to the currently selected rows)
   * @return an ErrorResponse value object in case of errors, VOResponse if the operation is successfully completed
   */
  public Response deleteRecords(ArrayList persistentObjects) throws Exception {
    throw new java.lang.UnsupportedOperationException("Method deleteRecords() not yet implemented.");
  }

  /**
   * Callback method invoked when the user has clicked on the insert button
   * @param valueObject empty value object just created: the user can manage it to fill some attribute values
   */
  public void createValueObject(ValueObject valueObject) throws Exception { }


  /**
   * Callback method invoked when the data loading is completed.
   * @param error <code>true</code> if data loading has terminated with errors, <code>false</code> otherwise
   */
  public void loadDataCompleted(boolean error) {
  }


  /**
   * Callback method invoked when the user has selected another row.
   * @param rowNumber selected row index
   */
  public void rowChanged(int rowNumber) {
  }


  /**
   * Callback method invoked after data reloading (on pressing reload/cancel button).
   * The method is called ONLY if the operation is successfully completed.
   */
  public void afterReloadGrid() {
  }


  /**
   * Callback method invoked on pressing COPY button.
   * @return <code>true</code> allows to go to INSERT mode (by copying data), <code>false</code> the mode change is interrupted
   */
  public boolean beforeCopyGrid(GridControl grid) {
    return true;
  }


  /**
   * Callback method invoked on pressing EDIT button.
   * @return <code>true</code> allows to go to EDIT mode, <code>false</code> the mode change is interrupted
   */
  public boolean beforeEditGrid(GridControl grid) {
    return true;
  }


  /**
   * Callback method invoked on pressing INSERT button.
   * @return <code>true</code> allows to go to INSERT mode, <code>false</code> the mode change is interrupted
   */
  public boolean beforeInsertGrid(GridControl grid) {
    return true;
  }


  /**
   * Callback method invoked before deleting data when the grid was in READONLY mode (on pressing delete button).
   * @return <code>true</code> allows the deleting to continue, <code>false</code> the deleting is interrupted
   */
  public boolean beforeDeleteGrid(GridControl grid) {
    return true;
  }


  /**
   * Callback method invoked after saving data when the grid was in EDIT mode (on pressing save button).
   * The method is called ONLY if the operation is successfully completed.
   */
  public void afterEditGrid(GridControl grid) {
  }


  /**
   * Callback method invoked after saving data when the grid was in INSERT mode (on pressing save button).
   * The method is called ONLY if the operation is successfully completed.
   */
  public void afterInsertGrid(GridControl grid) {
  }


  /**
   * Callback method invoked after deleting data when the grid was in READONLY mode (on pressing delete button).
   * The method is called ONLY if the operation is successfully completed.
   */
  public void afterDeleteGrid() {
  }


  /**
   * Callback method invoked each time the grid mode is changed.
   * @param currentMode current grid mode
   */
  public void modeChanged(int currentMode) { }


  /**
   * @param grid grid
   * @param row selected row index
   * @param attributeName attribute name that identifies the selected grid column
   * @return <code>true</code> if the selected cell is editable, <code>false</code> otherwise
   */
  public boolean isCellEditable(GridControl grid,int row,String attributeName) {
    return grid.isFieldEditable(row,attributeName);
  }


  /**
   * Method used to define the font to use for each cell of the grid.
   * @param rowNumber selected row index
   * @param attributeName attribute name related to the column currently selected
   * @param value object contained in the selected cell
   * @param defaultFont default font currently in used with this column
   * @return font to use for the current cell; null means default font usage; default value: null
   */
  public Font getFont(int row,String attributeName,Object value,Font defaultFont) {
    return null;
  }





  /************************************************************
   * DRAG 'N DROP MANAGEMENT
   ************************************************************/

  /**
   * Method called on beginning a drag event.
   * @return <code>true</code>, dragging can continue, <code>false</code> drag is not allowed; default value: <code>true</code>
   */
  public boolean dragEnabled() {
    return true;
  }


  /**
   * Method called on firing a drop event onto the grid.
   * @param gridId identifier of the source grid (grid that generate a draf event)
   * @return <code>true</code>, drop is allowed, <code>false</code> drop is not allowed; default value: <code>true</code>
   */
  public boolean dropEnabled(String gridId) {
    return true;
  }


  /**
   * This message goes to DragSourceListener, informing it that the dragging has entered the DropSite.
   */
  public void dragEnter() {}


  /**
   * This message goes to DragSourceListener, informing it that the dragging has exited the DropSite.
   */
  public void dragExit() {}


  /**
   * This message goes to DragSourceListener, informing it that the dragging is currently ocurring over the DropSite.
   */
  public void dragOver() {}


  /**
   * This method is invoked when the user changes the dropAction.
   */
  public void dropActionChanged() {}


  /**
   * This message goes to DragSourceListener, informing it that the dragging has ended.
   */
  public void dragDropEnd() {}



  /************************************************************
   * DROP MANAGEMENT
   ************************************************************/

  /**
   * This method is invoked when you are dragging over the DropSite.
   */
  public void dropEnter() {}


  /**
   * This method is invoked when you are exit the DropSite without dropping.
   */
  public void dropExit () {}


  /**
   * This method is invoked when a drag operation is going on.
   */
  public void dropOver (int row) {}












}