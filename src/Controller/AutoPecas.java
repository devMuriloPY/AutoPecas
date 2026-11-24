/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Controller;

import UI.Tema;
import View.LoginView;

/**
 *
 * @author mendonca
 */
public class AutoPecas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Tema.instalar(); // cores e fontes do sistema, antes de abrir qualquer janela
        java.awt.EventQueue.invokeLater(() -> new LoginView().setVisible(true));
    }

}
