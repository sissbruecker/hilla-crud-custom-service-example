import {ProductDtoCrudService, SupplierService} from 'Frontend/generated/endpoints.js';
import {useEffect, useState} from 'react';
import {AutoCrud} from "@hilla/react-crud";
import ProductDtoModel from "Frontend/generated/com/example/application/ProductDtoModel";
import Supplier from "Frontend/generated/com/example/application/Supplier";
import {Select} from "@hilla/react-components/Select";

export default function HelloWorldView() {
    const [suppliers, setSuppliers] = useState<Supplier[]>([]);
    const supplierOptions = suppliers.map(supplier => ({label: supplier.supplierName, value: String(supplier.id)}));

    useEffect(() => {
        SupplierService.listAll().then(setSuppliers);
    }, []);

    return (
        <div>
            <AutoCrud
                service={ProductDtoCrudService}
                model={ProductDtoModel}
                itemIdProperty={'productId'}
                gridProps={{
                    visibleColumns: ['productName', 'productCategory', 'productPrice', 'supplierInfo']
                }}
                formProps={{
                    visibleFields: ['productName', 'productCategory', 'productPrice', 'supplierId'],
                    fieldOptions: {
                        supplierId: {
                            renderer({field}) {
                                return <Select items={supplierOptions} {...field} label="Supplier"/>;
                            }
                        }
                    }
                }}
            />
        </div>
    );
}
