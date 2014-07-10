package jsf;

import ejb.BookCopyFacade;
import ejb.TransactionFacade;
import entity.Book;
import entity.BookCopy;
import entity.Staff;
import entity.Tx;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import javax.persistence.TemporalType;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;

@Named("transactionController")
@SessionScoped
public class TransactionController implements Serializable {

    @EJB
    private ejb.TransactionFacade ejbFacade;
    private List<Tx> items = null;
    private Tx selected;

    BookCopy bookCopy;
    Book book;
    Staff staff;
    Date dueDate;
    Date issueDate;

    List<Tx> issuedBooks;

    @EJB
    BookCopyFacade bookCopyFacade;

    public void acceptReturns() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Book?");
            return;
        }
        selected.setReturned(true);
        //selected.setReturnedDate(new Date());
        getFacade().edit(selected);
        selected.getBookCopy().setActive(true);
        getBookCopyFacade().edit(selected.getBookCopy());
        JsfUtil.addSuccessMessage("Acepted the book return");
    }

    public void issue() {
        if (bookCopy == null) {
            JsfUtil.addErrorMessage("Book?");
            return;
        }
        if (staff == null) {
            JsfUtil.addErrorMessage("Staff?");
            return;
        }
        selected = new Tx();
        selected.setBookCopy(bookCopy);
        selected.setActive(true);
        selected.setDueDate(dueDate);
        selected.setIssuedDate(issueDate);
        selected.setReturned(false);
        selected.setStaff(staff);
        bookCopy.setActive(false);
        getBookCopyFacade().edit(bookCopy);
        getFacade().create(selected);
        bookCopy = null;
        JsfUtil.addSuccessMessage("Issued");
    }

    public void fillIssuedBooks() {
        String sql = "Select t from Tx t where t.returned=false order by t.issuedDate";
        issuedBooks = getFacade().findByJpql(sql);
    }
    
    public void fillStaffIssuedBooks() {
        Map m = new HashMap();
        m.put("s", staff);
        String sql;
        sql = "Select t from Tx t where t.staff=:s order by t.issuedDate";
        issuedBooks = getFacade().findByJpql(sql,m);
    }

    public void fillBookcopyIssued() {
        Map m = new HashMap();
        m.put("bc", bookCopy);
        String sql = "Select t from Tx t where t.bookCopy=:bc order by t.issuedDate";
        issuedBooks = getFacade().findByJpql(sql,m);
    }

    public void fillBookIssued() {
        Map m = new HashMap();
        m.put("b", book);
        String sql = "Select t from Tx t where t.bookCopy.book = :b order by t.issuedDate";
        issuedBooks = getFacade().findByJpql(sql,m);
    }

    public Date getIssueDate() {
        if (issueDate == null) {
            issueDate = new Date();
        }
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public TransactionFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(TransactionFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public Date getDueDate() {
        if (dueDate == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(getIssueDate());
            c.add(Calendar.DATE, 14);
            dueDate = c.getTime();
        }
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public BookCopyFacade getBookCopyFacade() {
        return bookCopyFacade;
    }

    public void setBookCopyFacade(BookCopyFacade bookCopyFacade) {
        this.bookCopyFacade = bookCopyFacade;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public TransactionController() {
    }

    public Tx getSelected() {
        return selected;
    }

    public void setSelected(Tx selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private TransactionFacade getFacade() {
        return ejbFacade;
    }

    public Tx prepareCreate() {
        selected = new Tx();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("TransactionCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("TransactionUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("TransactionDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Tx> getItems() {
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

    public Tx getTransaction(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Tx> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Tx> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public List<Tx> getIssuedBooks() {
        return issuedBooks;
    }

    public void setIssuedBooks(List<Tx> issuedBooks) {
        this.issuedBooks = issuedBooks;
    }

    @FacesConverter(forClass = Tx.class)
    public static class TransactionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            TransactionController controller = (TransactionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "transactionController");
            return controller.getTransaction(getKey(value));
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
            if (object instanceof Tx) {
                Tx o = (Tx) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Tx.class.getName()});
                return null;
            }
        }

    }

}
