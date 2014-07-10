package jsf;

import entity.Staff;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;
import ejb.StaffFacade;
import entity.BookCopy;
import entity.Tx;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@Named("staffController")
@SessionScoped
public class StaffController implements Serializable {

    @EJB
    private ejb.StaffFacade ejbFacade;
    private List<Staff> items = null;
    private Staff selected;
    List<Staff> completeStaff;

    
    public List<Staff> staff(String sql){
        sql = "Select t from Tx t where t.staff=:s order by t.issuedDate";
        return getFacade().findByJpql(sql);
    }
    
    
    
    public List<Staff> getCompleteStaff() {
        return completeStaff;
    }

    public void setCompleteStaff(List<Staff> completeStaff) {
        this.completeStaff = completeStaff;
    }

    
//    public List<Institution> completeHospitals(String qry) {
//        String temSql;
//        if (getSessionController().getLoggedUser().getRestrictedInstitution() == null) {
//            temSql = "SELECT i FROM Institution i where i.retired=false and LOWER(i.name) like '%" + qry.toLowerCase() + "%' order by i.name";
//            return getFacade().findBySQL(temSql);
//        } else {
//            Map m = new HashMap();
//            m.put("mi", getSessionController().getLoggedUser().getRestrictedInstitution());
//            temSql = "SELECT i FROM Institution i where i=:mi";
//            return getFacade().findBySQL(temSql, m);
//        }
    
    
    public StaffController() {
    }

    public Staff getSelected() {
        return selected;
    }

    public void setSelected(Staff selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private StaffFacade getFacade() {
        return ejbFacade;
    }

    public Staff prepareCreate() {
        selected = new Staff();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("StaffCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("StaffUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("StaffDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Staff> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Staff getStaff(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Staff> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Staff> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Staff.class)
    public static class StaffControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StaffController controller = (StaffController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "staffController");
            return controller.getStaff(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Staff) {
                Staff o = (Staff) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Staff.class.getName()});
                return null;
            }
        }

    }

}
