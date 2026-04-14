/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package module.bussiness.product;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author An
 */
@WebServlet(name = "product", urlPatterns = {"/product"})
public class ProductController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ProductController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ProductController at" + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset = UTF-8");
        
        try(PrintWriter out = response.getWriter()){
            String id  = request.getParameter("id");
            if(id != null && !id.isBlank()){
                ProductEntity item = ProductService.getProductById(id);
                if(item == null){
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("Product not found");
                    return;
                }
                out.println("Product: " + item.getUserId() + " | " + item.getName());
                return;
            }
            
            List<ProductEntity> products = ProductService.getAllProducts();
            out.println("Product count: " + products.size());
            for(ProductEntity p : product){
                out.println(p.getUserId() + " | "+ p.getName());
            }

        }catch(SQLException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        CreateProduct dto = new CreateProduct();
        dto.setName(request.getParameter("name"));
        dto.setDescription(request.getParameter("description"));
        dto.setBrandId(request.getParameter("brandId"));
        dto.setStatus(request.getParameter("status"));
        dto.setUserId(request.getParameter("userId"));
        dto.setCategory(request.getParameter("category"));

        try(PrintWriter out = response.getWriter()){
            boolean ok = ProductService.createProduct(dto);
            if(!ok){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Create product failed");
                return;
            }
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.println("Create product success");
        }catch(SQLException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
     @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String id = request.getParameter("id");
        UpdateProduct dto = new UpdateProduct();
        dto.setName(request.getParameter("name"));
        dto.setDescription(request.getParameter("description"));
        dto.setBrandId(request.getParameter("brandId"));
        dto.setStatus(request.getParameter("status"));
        dto.setCategory(request.getParameter("category"));

        try (PrintWriter out = response.getWriter()) {
            boolean ok = productService.updateProduct(id, dto);
            if (!ok) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Update product failed");
                return;
            }
            out.println("Update product success");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        String id = request.getParameter("id");
        try (PrintWriter out = response.getWriter()) {
            boolean ok = productService.deleteProduct(id);
            if (!ok) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Delete product failed");
                return;
            }
            out.println("Delete product success");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
