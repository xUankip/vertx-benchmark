module com.example.vertxbenchmark {
//    requires javafx.controls;
//    requires javafx.fxml;
    requires io.vertx.web;
    requires io.vertx.core;
    requires io.vertx.client.sql.pg;
    requires io.vertx.client.sql;


    opens com.example.vertxbenchmark to javafx.fxml;
    exports com.example.vertxbenchmark;
}