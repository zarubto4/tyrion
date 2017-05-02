package utilities.financial;

import models.Model_Product;
import models.Model_ProductExtension;
import models.Model_Project;
import utilities.enums.Enum_ExtensionType;

import java.util.List;
import java.util.stream.Collectors;

public class FinancialPermission {

    public static boolean check(Model_Product product, String action){

        switch (action) {

            case "Project": {

                List<Model_ProductExtension> filtered = product.extensions.stream().filter(extension -> extension.type == Enum_ExtensionType.Project).collect(Collectors.toList());

                int available = 0;

                for (Model_ProductExtension extension : filtered) {
                    available += extension.count();
                }

                return Model_Project.find.where().eq("product.id", product.id).findRowCount() < available;
            }

            case "RestApi": {

                List<Model_ProductExtension> filtered = product.extensions.stream().filter(extension -> extension.type == Enum_ExtensionType.Project).collect(Collectors.toList());

                int available = 0;

                for (Model_ProductExtension extension : filtered) {
                    available += extension.count();
                }

                return Model_Project.find.where().eq("product.id", product.id).findRowCount() < available;
            }

            default: return false;
        }
    }
}
