package WebService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/*
 * @Author:Yu Gu AndrewID: ygu3
 *
 * The WebServlet class is the controller in this web service, which is used for
 * 1. Act as an api and handle get requests from mobile app
 * 2. Perform operation analytics and set the parameters in the dashboard view
 */
@jakarta.servlet.annotation.WebServlet(name = "WebServlet",
        urlPatterns = {"/getDriverStandings", "/Dashboard"})
public class WebServlet extends HttpServlet {


    // Initiate this servlet by instantiating the model that it will use.
    @Override
    public void init() {
    }

    // This servlet will reply to HTTP GET requests via this doGet method
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String nextView = "index.jsp";
        if (path.contains("getDriverStandings")) {
            String year = request.getParameter("year");
            String position = request.getParameter("position");
            // input parameter year checker
            try {
                int year_int = Integer.parseInt(year);
            } catch (Exception e) {
                System.out.println("Invalid year");
            }

            if (year != null) {
                // input parameter position checker
                int position_int;
                try {
                    if (position != null) {
                        position_int = Integer.parseInt(position);
                    }
                } catch (Exception e) {
                    System.out.println("Invalid position");
                }
                if (position == null) position = "";
                // get the response string and set the result parameter
                String result = WebModel.search(year, position);
                request.setAttribute("result", result);
                nextView = "result.jsp";
            } else {
                System.out.println("Null input");
            }

        }
        if (path.contains("Dashboard")) { // Set the parameter in dashboard view
            WebModel.getDBData();

            // get year search frequency rankings
            String yearResult = null;
            try {
                yearResult = WebModel.getFrequentYear().toString();
            } catch (NullPointerException e) {
                System.out.println("No such data");
            }
            // get position search frequency rankings
            String positionResult = null;
            try {
                positionResult = WebModel.getFrequentPosition().toString();
            } catch (NullPointerException e) {
                System.out.println("No such data");
            }
            // get the highest points
            String highestPoint = null;
            try {
                highestPoint = WebModel.getHighestPoints().toString();
            } catch (NullPointerException e) {
                System.out.println("No such data");
            }

            request.setAttribute("logs", WebModel.logs);
            request.setAttribute("pointResult", highestPoint);
            request.setAttribute("yearResult", yearResult);
            request.setAttribute("positionResult", positionResult);
            nextView = "dashboard.jsp";
        }

        // Transfer control over the correct "view"
        RequestDispatcher view = request.getRequestDispatcher(nextView);
        view.forward(request, response);
    }
}