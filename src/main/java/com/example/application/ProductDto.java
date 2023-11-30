package com.example.application;

//tag::snippet[]
public record ProductDto(Long productId,
                         String productName,
                         String productCategory,
                         double productPrice,
                         Long supplierId,
                         String supplierInfo) {
    public static ProductDto fromEntity(Product product) {
        String supplierInfo = product.getSupplier() != null
                ? String.format("%s (%s)", product.getSupplier().getSupplierName(), product.getSupplier().getHeadquarterCity())
                : "";
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getSupplier().getId(),
                supplierInfo
        );
    }
}
//end::snippet[]
