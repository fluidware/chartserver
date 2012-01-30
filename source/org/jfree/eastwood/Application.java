package org.jfree.eastwood;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

import org.jfree.chart.JFreeChart;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.jfree.chart.ChartUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.bio.SocketConnector;

public class Application {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        Connector connector=new SocketConnector();
        int port = 8180;
        if (args.length > 0){
            port = Integer.parseInt(args[0]);
        }
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        Handler handler=new RequestHandler();

        server.setHandler(handler);

        server.start();
        server.join();
    }

    public static class RequestHandler extends AbstractHandler
    {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {
            Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
            base_request.setHandled(true);

            Map params = request.getParameterMap();

            OutputStream out = response.getOutputStream();

            try {
                
                JFreeChart chart = ChartEngine.buildChart(params, new Font("arial", Font.PLAIN, 12));
                chart.setAntiAlias(true);
                String[] format_p = ((String[])params.get("format"));
                String format = "png";
                if (format_p != null) {
                    format = format_p[0];
                }

                // *** CHART SIZE ***
                String[] size = (String[]) params.get("chs");
                int[] dims = new int[2];
                if (size != null) {
                    dims = parseChartDimensions(size[0]);
                }
                else {
                    dims = new int[] {200, 125};
                }
                
                // *** CHART SCALE ***
                int scale = Integer.valueOf(params.containsKey("chscale") ? ((String[]) params.get("chscale"))[0]: "1");

                if (chart != null) {
                    if (format.toLowerCase().equals("svg")){
                        response.setContentType("image/svg+xml");
                        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                        Document document = domImpl.createDocument(null, "svg", null);
                        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
                        chart.draw(svgGenerator, new Rectangle2D.Double(0,0,dims[0],dims[1]), null);
                        Writer svgWriter = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
                        svgGenerator.stream(svgWriter, true);
                    } else if (format.toLowerCase().equals("jpg")) {
                        response.setContentType("image/jpeg");
                        ChartUtilities.writeChartAsJPEG(out, chart, dims[0], dims[1]);
                    } else {
                        response.setContentType("image/png");
                        ChartUtilities.writeScaledChartAsPNG(out, chart, dims[0], dims[1], scale, scale);
                        //ChartUtilities.writeChartAsPNG(out, chart, dims[0], dims[1]); 
                   }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                out.close();
            }

        }

        private int[] parseChartDimensions(String text) throws ServletException {
            if (text == null) {
                throw new IllegalArgumentException(
                        "Null 'text' argument (in parseChartDimensions(String)).");
            }
            int[] result = new int[2];
            int splitIndex = text.indexOf('x');
            String xStr = text.substring(0, splitIndex);
            String yStr = text.substring(splitIndex + 1);
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            if (x * y < 16000000) {
                result[0] = x; result[1] = y;
            }
            else {
                throw new ServletException("Invalid chart dimensions: " + xStr
                        + ", " + yStr);
            }
            return result;
        }
    }

}
