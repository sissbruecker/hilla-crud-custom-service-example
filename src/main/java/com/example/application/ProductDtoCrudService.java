package com.example.application;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.hilla.Nullable;
import dev.hilla.crud.CrudService;
import dev.hilla.crud.filter.AndFilter;
import dev.hilla.crud.filter.Filter;
import dev.hilla.crud.filter.OrFilter;
import dev.hilla.crud.filter.PropertyStringFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

@BrowserCallable
@AnonymousAllowed
public class ProductDtoCrudService implements CrudService<ProductDto, Long> {
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ProductDtoCrudService(ProductRepository productRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    public List<ProductDto> list(Pageable pageable, @Nullable Filter filter) {
        // Create page request with mapped sort properties
        pageable = createPageRequest(pageable);
        // Create JPA specification from Hilla filter
        Specification<Product> specification = createSpecification(filter);
        // Fetch data from JPA repository
        return productRepository.findAll(specification, pageable)
                .map(ProductDto::fromEntity)
                .toList();
    }

    private Pageable createPageRequest(Pageable pageable) {
        // Map sort orders to JPA property names
        List<Sort.Order> sortOrders = pageable.getSort().stream()
                .map(order -> {
                    String mappedProperty = switch (order.getProperty()) {
                        case "productName" -> "name";
                        case "productCategory" -> "category";
                        case "productPrice" -> "price";
                        case "supplierInfo" -> "supplier.supplierName";
                        default -> throw new IllegalArgumentException("Unknown sort property " + order.getProperty());
                    };
                    return order.isAscending()
                            ? Sort.Order.asc(mappedProperty)
                            : Sort.Order.desc(mappedProperty);
                }).toList();
        // Create page request with mapped sort properties
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortOrders));
    }

    private Specification<Product> createSpecification(Filter filter) {
        if (filter == null) {
            return Specification.anyOf();
        }
        if (filter instanceof AndFilter andFilter) {
            return Specification.allOf(andFilter.getChildren().stream()
                    .map(this::createSpecification).toList());
        } else if (filter instanceof OrFilter orFilter) {
            return Specification.anyOf(orFilter.getChildren().stream()
                    .map(this::createSpecification).toList());
        } else if (filter instanceof PropertyStringFilter propertyFilter) {
            return filterProperty(propertyFilter);
        } else {
            throw new IllegalArgumentException("Unknown filter type " + filter.getClass().getName());
        }
    }

    private static Specification<Product> filterProperty(PropertyStringFilter filter) {
        String filterValue = filter.getFilterValue();

        return (root, query, criteriaBuilder) -> {
            return switch (filter.getPropertyId()) {
                case "productName" -> criteriaBuilder.like(root.get("name"), "%" + filterValue + "%");
                case "productCategory" -> criteriaBuilder.like(root.get("category"), "%" + filterValue + "%");
                case "productPrice" -> switch (filter.getMatcher()) {
                    case EQUALS -> criteriaBuilder.equal(root.get("price"), filterValue);
                    case GREATER_THAN -> criteriaBuilder.greaterThan(root.get("price"), filterValue);
                    case LESS_THAN -> criteriaBuilder.lessThan(root.get("price"), filterValue);
                    default -> throw new IllegalArgumentException("Unsupported matcher: " + filter.getMatcher());
                };
                case "supplierInfo" -> criteriaBuilder.or(
                        criteriaBuilder.like(root.get("supplier").get("supplierName"), "%" + filterValue + "%"),
                        criteriaBuilder.like(root.get("supplier").get("headquarterCity"), "%" + filterValue + "%")
                );
                default -> null;
            };
        };
    }

    @Override
    public @Nullable ProductDto save(ProductDto value) {
        // Get existing product or create a new one
        Product product;
        if (value.productId() != null && value.productId() > 0) {
            product = productRepository.getReferenceById(value.productId());
        } else {
            product = new Product();
            product.setDateAdded(LocalDate.now());
        }
        // Update product properties
        product.setName(value.productName());
        product.setCategory(value.productCategory());
        product.setPrice(value.productPrice());
        // Update supplier
        Supplier supplier = supplierRepository.getReferenceById(value.supplierId());
        product.setSupplier(supplier);
        // Save and return updated product
        return ProductDto.fromEntity(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public ProductDto get(Long aLong) {
        return null;
    }
}
