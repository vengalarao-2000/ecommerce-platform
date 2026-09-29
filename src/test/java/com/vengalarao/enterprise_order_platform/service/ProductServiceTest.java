package com.vengalarao.enterprise_order_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vengalarao.enterprise_order_platform.dto.CreateProductRequest;
import com.vengalarao.enterprise_order_platform.dto.ProductResponse;
import com.vengalarao.enterprise_order_platform.dto.UpdateProductRequest;
import com.vengalarao.enterprise_order_platform.entity.Product;
import com.vengalarao.enterprise_order_platform.exception.ProductAlreadyExistsException;
import com.vengalarao.enterprise_order_platform.exception.ProductNotFoundException;
import com.vengalarao.enterprise_order_platform.repository.ProductRepository;

@ExtendWith(MockitoExtension.class) //This annotation tells to use Mockito while running this test class.
public class ProductServiceTest {
    @Mock  //Create fake ProductRepository
    private ProductRepository productRepository;

    @InjectMocks //Mockito creates the service and supplies the mock repository to its constructor.
    private ProductService productService;

    @Test  //Tells JUnit that this is a test case.
    //Methid naming convention: methodName_scenario_expectedResult
    void createProduct_success() {
        //Arrange: Prepare everything
        CreateProductRequest request = new CreateProductRequest("Wireless mouse", "Bluetooth mouse", new BigDecimal(12.99), 50);
        //The below line means, when the mockRepository receives existsByNameIgnoreCase("Wireless mouse"), return false.
        //THis ensures No SQL is executed, No DB is checked, Mockito simply returns the configured values.
        when(productRepository.existsByNameIgnoreCase("Wireless mouse")).thenReturn(false);
        //Any product that save method gets, set its id to 1L and return the product.
        when(productRepository.save(any(Product.class)))
        .thenAnswer(invocation -> {
            //invocation object represents the method call (has information like save, argument passed to save here we only have one). First argument to save is product object.
            Product product = invocation.getArgument(0);
            product.setId(1L);
            return product;
        });

        //Act: Method we are testing? Execute the method
        ProductResponse response = productService.createProduct(request);

        //Assert: Verify the results.
        assertEquals(1L, response.getId());
        assertEquals("Wireless mouse", response.getName());
        assertEquals(new BigDecimal(12.99), response.getPrice());
        assertEquals(50, response.getStockQuantity());
        //Verifies the business rule i.e newly created product is active by default.
        assertTrue(response.getActive());

        //Checks interactions with the mock. Verify that productRepository.save() is called exactly once with any product instance.
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_duplicateName_throwsException() {
  /*       Given:
        A product named "Wireless Mouse" already exists

        When:
        createProduct() is called with the same name

        Then:
        ProductAlreadyExistsException should be thrown
        and repository.save() should never be called */

        //1. Arrange
        CreateProductRequest request = new CreateProductRequest("Wireless Mouse", "Bluetooth mouse", new BigDecimal(12.99), 40);
        //As we want to test this scenario where product name exists in the DB.
        //Tells mockito that product already exists
        when(productRepository.existsByNameIgnoreCase("Wireless Mouse")).thenReturn(true);

        //2. Act + Assert
        //Run productService.createProduct(request) and confirm it throws ProductAlreadyExistsException
        //If no exception is thrown OR exception mismatch with what we are expecting then the test will fail.
        //If expected exception is thrown, JUnit returns that exception so we can inspect.
        ProductAlreadyExistsException exception = assertThrows(ProductAlreadyExistsException.class, () -> productService.createProduct(request));
        assertEquals("Product with name 'Wireless Mouse' already exists", exception.getMessage());
        
        //3. Verify
        //Verify that save was never called.
        verify(productRepository, never()).save(any(Product.class));
        verify(productRepository, times(1)).existsByNameIgnoreCase("Wireless Mouse");
    }

    @Test
    void getProductById_success() {
        /* Two Scenarios:
        i. Product exists: return ProductResponse
        ii. Product not found: throw ProductNotFoundException */

        //Arrange:
        Product product = new Product(
            1L,
            "Wireless Mouse",
            "Bluetooth Mouse",
            new BigDecimal("39.99"),
            50,
            true
        );
        //We are telling Mockito that when service looks for product ID = 1, pretend the database found this product.
        //As our real repository method returns Optional so, even in below case we are returning the same.
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        //Act
        ProductResponse response = productService.getProductById(1L);

        //Assert
        assertEquals(1L, response.getId());
        assertEquals("Wireless Mouse", response.getName());
        assertEquals("Bluetooth Mouse", response.getDescription());
        assertEquals(50, response.getStockQuantity());
        assertEquals(true, response.getActive());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_notFound_throwsException() {
        //Arrange
        //empty() means product doesn't exist
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        //Act + Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productService.getProductById(999L));

        assertEquals("Product with id 999 does not exist.", exception.getMessage());

        verify(productRepository, times(1)).findById(999L);

    }

    @Test
    void getAllProducts_success() {
        //Let's assume that the repo has 2 products and returns both of them

        //Arrange
        Product prod1 = new Product(
            1L,
            "Wireless Mouse",
            "Bluetooth mouse",
            new BigDecimal(39.99),
            40,
            true
        );

        Product prod2 = new Product(
            2L,
            "Wireless Keyboard",
            "Bluetooth keyboard",
            new BigDecimal(99.99),
            10,
            false
        );

        List<Product> products = List.of(prod1, prod2);
        //Set the mock ProductRepository to return dummy product objects we created upon calling the findAll()
        when(productRepository.findAll()).thenReturn(products);

        //Act
        //The service method getAllProducts() takes care of the conversion from Product to ProductResponse.
        //This service method call when invoked repository the dummy list we created is returned.
        List<ProductResponse> response = productService.getAllProducts();

        //Assert
        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(true, response.get(0).getActive());
        assertEquals("Wireless Mouse", response.get(0).getName());
        assertEquals(2L, response.get(1).getId());
        assertEquals(false, response.get(1).getActive());
        assertEquals(10, response.get(1).getStockQuantity());

        verify(productRepository, times(1)).findAll();

    }

    @Test
    void getAllProducts_emptyList() {
        //Arrange
        //Return an epty list when invoked mock repo's findAll() method
        when(productRepository.findAll()).thenReturn(List.of());

        //Act
        List<ProductResponse> response = productService.getAllProducts();

        //Assert
        assertEquals(0, response.size());
        verify(productRepository, times(1)).findAll();
    }

    /* Update Product Tests: has 3 Scenarios
    - Successful Update
    - Product ID not found
    - duplicate name conflict
     */
    @Test
    void updateProduct_success() {
        //Arrange
        Long productId = 1L;

        Product existingProduct = new Product(
            productId,
            "Wireless Mouse",
            "Bluetooth Mouse",
            new BigDecimal(89.10),
            10,
            true
        );

        UpdateProductRequest request = new UpdateProductRequest(
            "Updated Mouse",
            "Updt Description",
            new BigDecimal(99.10),
            10,
            true
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        //This means newly updated name should not exist in the DB with a different Id. If we are not updaing the name then name will be same but IdNot excepts different ids there by fails and returns false.
        when(productRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), productId)).thenReturn(false);
        /* When called save during update the id remains same and all fields are updated based on request object passed to update project
        Hence we are simply returning the object from invocation without altering it.
        i.e createProduct_success unit test the DB creates an id so we set the id to 1L then returned*/
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        ProductResponse response = productService.updateProduct(productId, request);

        //Assert
        assertEquals(productId, response.getId());
        assertEquals("Updated Mouse", response.getName());
        assertEquals("Updt Description", response.getDescription());
        assertEquals(new BigDecimal(99.10), response.getPrice());
        assertEquals(10, response.getStockQuantity());
        assertEquals(true, response.getActive());

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).existsByNameIgnoreCaseAndIdNot(request.getName(), productId);
        //Notice we are saving existing product. In service, we retrieve the product, update the fields of
        //retrieved product using UpdateProduct request object and retrieved product. We'll not save UpdateProductRequest object.
        //Follwing the same here as well.
        verify(productRepository, times(1)).save(existingProduct);
    }

    //Update Product Id doesn't exist
    @Test
    void updateProduct_notFound_throwsException() {
        //Arrange
        Long productId = 999L;

        UpdateProductRequest request = new UpdateProductRequest(
            "Updated Wireless Mouse",
            "Updt description",
            new BigDecimal(49.99),
            10,
            false
        );

        //When called repository method to findById() return Optional.empty() which means requested object is not found.
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        //Act + Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(productId, request));

        assertEquals("Product with id " + productId + " not found", exception.getMessage());

        verify(productRepository, times(1)).findById(productId);
        //As service layer throws an exception as soon as request productId is not found, we'll never all the productRepo's save().
        verify(productRepository, never()).save(any(Product.class));
        //Once productId is not found, the service stops immediately hence existsByNameIgnoreCaseAndIdNot() should never get called.
        verify(productRepository, never()).existsByNameIgnoreCaseAndIdNot(anyString(), anyLong());

    }

    //Update Product duplicate name exists
    @Test
    void updateProduct_duplicateName_throwsException() {
        //Arrange
        Long id = 999L;

        Product existingProduct = new Product(
            id,
            "Wireless Mouse",
            "Bluetooth mouse",
            new BigDecimal(89.99),
            19,
            true
        );

        UpdateProductRequest request = new UpdateProductRequest(
            "Logitech Keyboard",
            "Updated Descr.",
            new BigDecimal(100.99),
            2,
            true
        );

        //Return existing product.
        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        //This scenario is all about Logictech Keyboard exists in DB with a different productId i.e say 2.
        //Then exception (ProductALreadyExistsException is thrown)
        when(productRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)).thenReturn(true);

        //Act + Assert
        ProductAlreadyExistsException exception = assertThrows(ProductAlreadyExistsException.class, () -> productService.updateProduct(id, request));
        //Expected VS Actual
        assertEquals("Product with name " + request.getName() + " already exists.", exception.getMessage());
        
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).existsByNameIgnoreCaseAndIdNot(request.getName(), id);
        verify(productRepository, never()).save(any(Product.class));
    }

    //Unit Tests for Delete: soft and hard delete and product not found scenario.
    @Test
    void deleteProduct_activeProduct_setsInactive() {
        //Arrange
        Long id = 1L;

        Product product = new Product (
            id,
            "Wireless Mouse",
            "Some desc",
            new BigDecimal(90.49),
            19,
            true
        );

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        //When called save, the invocation has method call, and object is saved in that object
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        //Act
        productService.deleteProduct(id);

        //Assert
        //After performing soft delete the active status must be set to false.
        assertFalse(product.getActive());

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(product);
        //As we are not removing the row completely, no call should be made to delete()
        verify(productRepository, never()).delete(any(Product.class));

    }

    @Test
    void deleteProduct_inactiveProduct_deletesProduct() {
        //Arrange
        Long id = 1L;

        Product product = new Product(
            id,
            "Wireless Mouse",
            "Some desc",
            new BigDecimal(90.89),
            10,
            false
        );

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        //Act
        productService.deleteProduct(id);

        //Assert
        //We don't have anything to assert as deleteProduct() returns nothing. So, interaction verification becomes important
        //i.e save() gets never called and findById() and delete() are called once.
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).delete(product);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_notFound_throwsException() {
        //Arrange
        Long id = 999L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        //Act + Assert
        ProductNotFoundException exception = assertThrows(
            ProductNotFoundException.class, () -> productService.deleteProduct(id));
        
        assertEquals("Product with id " + id + " does not exist", exception.getMessage());

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, never()).delete(any(Product.class));
        verify(productRepository, never()).save(any(Product.class));
        
    }








}
