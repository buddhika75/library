/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jsf;

import enums.Language;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author root
 */
@Named(value = "enumController")
@ApplicationScoped
public class EnumController {

    /**
     * Creates a new instance of EnumController
     */
    public EnumController() {
    }
    
    public Language[] getLanguages(){
        return Language.values();
    }
    
}
