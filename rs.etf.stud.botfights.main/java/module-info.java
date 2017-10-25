open module rs.etf.stud.botfights.main {
    requires rs.etf.stud.botfights.core;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.web;
    requires javafx.swing;
    requires com.jfoenix;
    requires jdk.xml.dom;

    uses rs.etf.stud.botfights.core.Game;

    exports rs.etf.stud.botfights.main;
    exports rs.etf.stud.botfights.main.controllers;
}