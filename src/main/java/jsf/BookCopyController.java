package jsf;

import entity.BookCopy;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;
import ejb.BookCopyFacade;
import entity.Book;

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

@Named("bookCopyController")
@SessionScoped
public class BookCopyController implements Serializable {

    @EJB
    private ejb.BookCopyFacade ejbFacade;
    private List<BookCopy> items = null;
    private BookCopy selected;

    public BookCopyController() {
    }

    public BookCopy getSelected() {
        return selected;
    }

    public List<BookCopy> completeBookCopies(String qry) {
        Map m = new HashMap();
        m.put("n", "%" + qry.toUpperCase() + "%");
        String j = "Select bc from BookCopy bc where (bc.book.name like :n or bc.name like :n or bc.book.isbnNumber like :n or  bc.book.author.name like :n) and bc.active=true order by bc.book.name ";
        return getFacade().findByJpql(j, m);
    }

    public void setSelected(BookCopy selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private BookCopyFacade getFacade() {
        return ejbFacade;
    }

    public BookCopy prepareCreate() {
        selected = new BookCopy();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("BookCopyCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("BookCopyUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("BookCopyDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<BookCopy> getItems() {
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

    public BookCopy getBookCopy(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<BookCopy> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<BookCopy> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = BookCopy.class)
    public static class BookCopyControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BookCopyController controller = (BookCopyController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "bookCopyController");
            return controller.getBookCopy(getKey(value));
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
            if (object instanceof BookCopy) {
                BookCopy o = (BookCopy) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), BookCopy.class.getName()});
                return null;
            }
        }

    }

}
