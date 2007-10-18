package org.openswing.swing.form.model.client;

import org.openswing.swing.client.*;
import java.lang.reflect.Method;
import java.beans.*;
import org.openswing.swing.message.receive.java.ValueObject;
import org.openswing.swing.logger.client.Logger;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import org.openswing.swing.form.client.Form;


/**
 * <p>Title: OpenSwing Framework</p>
 * <p>Description: Data model linked to a Form panel.</p>
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
public class VOModel {

  /** value object class associated to this data model */
  private Class valueObjectClass = null;

  /** valueObject info */
  private BeanInfo info;

  /** value object stored inside the model */
  private ValueObject valueObject = null;

  /** value changed listeners */
  private ArrayList valueChangeListeners = new ArrayList();

  /** collection of <attribute name,Method[] getters+setter> */
  private Hashtable voSetterMethods = new Hashtable();

  /** collection of <attribute name,Method[] getters> */
  private Hashtable voGetterMethods = new Hashtable();

  /** flag used to define if an inner v.o. must be automatically instantiated when a setter method is invoked */
  private boolean createInnerVO = true;

  /** linked Form */
  private Form form = null;


  /**
   * Constructor.
   * @param valueObjectClass value object class associated to this data model
   * @param createInnerVO flag used to define if an inner v.o. must be automatically instantiated when a setter method is invoked
   * @throws java.lang.Exception if an error occours
   */
  public VOModel(Class valueObjectClass,boolean createInnerVO,Form form) throws Exception {
    this.valueObjectClass = valueObjectClass;
    this.createInnerVO = createInnerVO;
    this.form = form;

    // retrieve attribute properties...
    if (Beans.isDesignTime())
      return;
    analyzeClassFields("",new Method[0],valueObjectClass);
    setValueObject( (ValueObject) valueObjectClass.newInstance());
  }


  /**
   * Define if an inner v.o. must be automatically instantiated when a setter method is invoked.
   * @param createInnerVO define if an inner v.o. must be automatically instantiated when a setter method is invoked
   */
  public final void setCreateInnerVO(boolean createInnerVO) {
    this.createInnerVO = createInnerVO;
  }


  /**
   * Analyze class fields and fill in "voSetterMethods","voGetterMethods","indexes",reverseIndexes" attributes.
   * @param prefix e.g. "attrx.attry."
   * @param parentMethods getter methods of parent v.o.
   * @param classType class to analyze
   */
  private void analyzeClassFields(String prefix,Method[] parentMethods,Class classType) {
    try {
      info = Introspector.getBeanInfo(classType);
      // retrieve attribute properties...
      PropertyDescriptor[] props = info.getPropertyDescriptors();
      for (int i = 0; i < props.length; i++) {

        if (props[i].getReadMethod()!=null &&
            props[i].getReadMethod().getParameterTypes().length==0 &&
            ValueObject.class.isAssignableFrom( props[i].getReadMethod().getReturnType() )
        ) {
          Method[] newparentMethods = new Method[parentMethods.length+1];
          System.arraycopy(parentMethods,0,newparentMethods,0,parentMethods.length);
          newparentMethods[parentMethods.length] = props[i].getReadMethod();
          analyzeClassFields(prefix+props[i].getName()+".",newparentMethods,props[i].getReadMethod().getReturnType());
        }
        Method[] newparentMethods = new Method[parentMethods.length+1];
        System.arraycopy(parentMethods,0,newparentMethods,0,parentMethods.length);
        newparentMethods[parentMethods.length] = props[i].getReadMethod();
        voGetterMethods.put(prefix+props[i].getName(),newparentMethods);

        Method[] newparentMethods2 = new Method[parentMethods.length+1];
        System.arraycopy(parentMethods,0,newparentMethods2,0,parentMethods.length);
        newparentMethods2[parentMethods.length] = props[i].getWriteMethod();
        voSetterMethods.put(prefix+props[i].getName(),newparentMethods2);
      }

    }
    catch (Throwable ex) {
      Logger.error(this.getClass().getName(),"analyzeClassFields","Error on analyzing the object",ex);
    }
  }



  /**
   * @return value object class associated to this data model
   */
  public final Class getValueObjectType() {
    return valueObjectClass;
  }


  /**
   * Set the value object that this model will use.
   * @param valueObject value object to store
   */
  public final void setValueObject(ValueObject valueObject) {
      if (valueObject != null &&
          !valueObjectClass.isAssignableFrom(valueObject.getClass())) {
//          !valueObject.getClass().equals(valueObjectClass)) {
        throw new RuntimeException("The specified value object has not type '"+valueObjectClass.toString()+"'");
      }
      Object oldValueObject = this.valueObject;
      this.valueObject = valueObject;
      if (valueObject==null && oldValueObject!=null ||
          valueObject!=null && oldValueObject==null ||
          valueObject!=null && !valueObject.equals(oldValueObject)) {
        // fire value changed events...
        PropertyDescriptor prop = null;
        Method[] readMethods = null;
        Object oldValue = null;
        Object newValue = null;
        String attrName = null;
        Enumeration en = voGetterMethods.keys();
        Object obj = null;
        while(en.hasMoreElements()) {
          attrName = en.nextElement().toString();
          readMethods = (Method[])voGetterMethods.get(attrName);
          if (readMethods != null) {
            try {
              obj = oldValueObject;
              if (obj!=null)
                for(int i=0;i<readMethods.length-1;i++) {
                  obj = readMethods[i].invoke(obj,new Object[0]);

                  // check if the inner v.o. is null...
                  if(obj == null) {
                    if (!createInnerVO)
                      break;
                    else
                      obj = (ValueObject)readMethods[i].getReturnType().newInstance();
                  }
                }
              oldValue = obj!=null?readMethods[readMethods.length-1].invoke(obj, new Object[0]):null;

              obj = valueObject;
              if (obj!=null)
                for(int i=0;i<readMethods.length-1;i++) {
                  obj = readMethods[i].invoke(obj,new Object[0]);

                  // check if the inner v.o. is null...
                  if(obj == null) {
                    if (!createInnerVO)
                      break;
                    else
                      obj = (ValueObject)readMethods[i].getReturnType().newInstance();
                  }
                }
              newValue = obj!=null?readMethods[readMethods.length-1].invoke(obj, new Object[0]):null;
            }
            catch (Exception ex) {
              Logger.error(this.getClass().getName(),"setValueObject","Error while setting the value object attribute '"+attrName+"'",ex);
              continue;
            }
          }
          if (newValue==null && oldValue!=null ||
              newValue!=null && oldValue==null ||
              newValue!=null && oldValue!=null && !newValue.equals(oldValue))
            fireValueChanged(attrName,oldValue,newValue);

        }
      }
  }


  /**
   * @return value object stored inside the model
   */
  public final ValueObject getValueObject() {
    return valueObject;
  }


  /**
   * @param attributeName attribute name of the value object
   * @return type of the value associated to the specified attribute name
   */
  public final Class getAttributeType(String attributeName) {
    try {
      Method[] readMethods = (Method[])voGetterMethods.get(attributeName);
      if (readMethods != null) {
        return readMethods[readMethods.length-1].getReturnType();
      }
      else
        return null;
    }
    catch (Throwable ex) {
      Logger.error(this.getClass().getName(),"getAttributeType","Error while analyzing the value object attribute '"+attributeName+"'",ex);
    }
    return null;
  }


  /**
   * @param attributeName attribute name of the value object
   * @return value value associated to the specified attribute name
   */
  public final Object getValue(String attributeName) {
      if (getValueObject() == null) {
        return null;
      }
      try {
        Method[] readMethods = (Method[])voGetterMethods.get(attributeName);
        if (readMethods != null) {
          Object obj = getValueObject();
          if (obj!=null)
            for(int i=0;i<readMethods.length-1;i++) {
              obj = readMethods[i].invoke(obj,new Object[0]);

              // check if the inner v.o. is null...
              if(obj == null) {
                if (!createInnerVO)
                  return null;
                else
                  obj = (ValueObject)readMethods[i].getReturnType().newInstance();
              }
            }
          return obj!=null?readMethods[readMethods.length-1].invoke(obj, new Object[0]):null;
        }
      }
      catch (Throwable ex) {
        Logger.error(this.getClass().getName(),"getValue","Error while reading the value object attribute '"+attributeName+"'",ex);
      }
      return null;
  }


  /**
   * @param attributeName attribute name of the value object
   * @param valueobject value object used to fetch the attribute value
   * @return value value associated to the specified attribute name
   */
  public final Object getValue(String attributeName,ValueObject valueobject) {
      if (valueobject == null) {
        return null;
      }
      try {
        Method[] readMethods = (Method[])voGetterMethods.get(attributeName);
        if (readMethods != null) {
          Object obj = valueobject;
          if (obj!=null)
            for(int i=0;i<readMethods.length-1;i++) {
              obj = readMethods[i].invoke(obj,new Object[0]);

              // check if the inner v.o. is null...
              if(obj == null) {
                if (!createInnerVO)
                  return null;
                else
                  obj = (ValueObject)readMethods[i].getReturnType().newInstance();
              }
            }
          return obj!=null?readMethods[readMethods.length-1].invoke(obj, new Object[0]):null;
        }
      }
      catch (Throwable ex) {
        Logger.error(this.getClass().getName(),"getValue","Error while reading the value object attribute '"+attributeName+"'",ex);
      }
      return null;
  }



  /**
   * Set a value in the value object for an attribute name.
   * @param attributeName attribute name of the value object
   * @return value value to set for the specified attribute name
   */
  public final void setValue(String attributeName,Object value) {
      if (getValueObject() == null) {
          return;
      }
      try {
        Method[] writeMethods = (Method[])voSetterMethods.get(attributeName);
        if (writeMethods != null) {
          Object oldValue = getValue(attributeName);
          if (value!=null) {
            if (writeMethods[writeMethods.length-1].getParameterTypes()[0].equals(java.sql.Date.class) &&
              value.getClass().equals(java.util.Date.class))
              value = new java.sql.Date(((java.util.Date)value).getTime());
            else if (writeMethods[writeMethods.length-1].getParameterTypes()[0].equals(java.sql.Timestamp.class) &&
              value.getClass().equals(java.util.Date.class))
              value = new java.sql.Timestamp(((java.util.Date)value).getTime());
            else if (writeMethods[writeMethods.length-1].getParameterTypes()[0].equals(java.sql.Timestamp.class) &&
              value.getClass().equals(java.sql.Date.class))
              value = new java.sql.Timestamp(((java.sql.Date)value).getTime());
            else if (writeMethods[writeMethods.length-1].getParameterTypes()[0].equals(Integer.class) &&
              value.getClass().equals(java.math.BigDecimal.class))
              value = new Integer(((java.math.BigDecimal)value).intValue());
            else if (writeMethods[writeMethods.length-1].getParameterTypes()[0].equals(Long.class) &&
              value.getClass().equals(java.math.BigDecimal.class))
              value = new Long(((java.math.BigDecimal)value).longValue());
            else if (writeMethods[writeMethods.length-1].getParameterTypes()[0].equals(Double.class) &&
              value.getClass().equals(java.math.BigDecimal.class))
              value = new Double(((java.math.BigDecimal)value).doubleValue());
            else if (writeMethods[writeMethods.length-1].getParameterTypes()[0].equals(Float.class) &&
              value.getClass().equals(java.math.BigDecimal.class))
              value = new Float(((java.math.BigDecimal)value).floatValue());
          }

          Object obj = getValueObject();
          if (obj!=null)
            for(int i=0;i<writeMethods.length-1;i++) {
              obj = writeMethods[i].invoke(obj,new Object[0]);

              // check if the inner v.o. is null...
              if(obj == null) {
                if (!createInnerVO)
                  return;
                else
                  obj = (ValueObject)writeMethods[i].getReturnType().newInstance();
              }
            }

          if (value==null && oldValue!=null ||
              value!=null && oldValue==null ||
              value!=null && oldValue!=null && !value.equals(oldValue)) {
            boolean isOk = form.getFormController().validateControl(
                attributeName,
                oldValue,
                value);
            if (isOk) {
              writeMethods[writeMethods.length-1].invoke(obj, new Object[]{value});
              fireValueChanged(attributeName,oldValue,value);
            }
            else {
              form.pull(attributeName);
            }
          }
        }
      }
      catch (Exception ex) {
        Logger.error(this.getClass().getName(),"setValue","Error while writing the value object attribute '"+attributeName+"'",ex);
      }
  }


  /**
   * Method used to add a value changed listener.
   * @param listener value changed listener to add
   */
  public final void addValueChangeListener(ValueChangeListener listener) {
    valueChangeListeners.add(listener);
  }


  /**
   * Method used to remove a value changed listener.
   * @param listener value changed listener to remove
   */
  public final void removeValueChangeListener(ValueChangeListener listener) {
    valueChangeListeners.remove(listener);
  }


  /**
   * @return list of value changed listeners added to this model
   */
  public final ValueChangeListener[] getValueChangeListeners() {
    return (ValueChangeListener[])valueChangeListeners.toArray(new ValueChangeListener[valueChangeListeners.size()]);
  }


  /**
   * Method called by setValue and setValueObject methods to fire value changed events.
   * @param attributeName attribute name
   * @param oldValue old value of the attribute
   * @param newValue new value of the attribute
   */
  private void fireValueChanged(String attributeName,Object oldValue,Object newValue) {
      ValueChangeListener[] formListeners = getValueChangeListeners();
      ValueChangeEvent e = new ValueChangeEvent(this,attributeName,oldValue,newValue);
      if (valueChangeListeners != null) {
          for (int i = 0; i < valueChangeListeners.size(); i++) {
              ValueChangeListener vcl = (ValueChangeListener)
                  valueChangeListeners.get(i);
              vcl.valueChanged(e);
          }
      }
  }



}
