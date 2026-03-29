package com.vms.util;

import com.vms.model.User;

public class RBACUtil {
    public static boolean peutAcceder(User user, String module) {
        if (user == null) return false;
        String role = user.getRole();
        switch (module) {
            case "DEMANDES":     return true;
            case "VOUCHERS":     return true;
            case "MAGASINS":     return true;
            case "APPROUVER":    return role.equals("APPROBATEUR") || role.equals("SUPER_USER");
            case "REDEMPTION":   return role.equals("ADMINISTRATEUR") || role.equals("SUPER_USER");
            case "CLIENTS":      return !role.equals("INITIATEUR");
            case "UTILISATEURS": return role.equals("SUPER_USER");
            default:             return false;
        }
    }
}