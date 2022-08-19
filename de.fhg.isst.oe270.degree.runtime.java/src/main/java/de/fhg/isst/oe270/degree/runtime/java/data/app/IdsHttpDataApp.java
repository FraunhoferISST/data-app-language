/*
 * Copyright 2020-2022 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhg.isst.oe270.degree.runtime.java.data.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This is a D° application which offers a HTTP-interface and IDS metadata endpoints.
 */
public abstract class IdsHttpDataApp extends HttpDataApp {

    /**
     * This key is used to identify the ids flag.
     */
    public static final String IDS_KEY = "ids";

    /**
     * Get the IDS metadata for this application from resources.
     *
     * @return the application's metadata
     */
    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/metadata", method = RequestMethod.GET)
    @ResponseBody
    @Operation(
            summary = "Get IDS metadata for this application in JSON format.",
            description = "Get the IDS infomodel entity which represents this application"
                    + " and contains all relevant meta data",
            tags = "IDS",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns data app's metadata as valid JSON-encoded"
                                    + " IDS-infomodel entity.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject("Insert Infomodel here...")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Returns an error if retrieval of data app's metadata"
                                    + " failed because of a not data app related error.",
                            content = @Content(
                                    mediaType = "application/text",
                                    examples = @ExampleObject(
                                            "<html>\n"
                                                    + "\t<body>\n"
                                                    + "\t\t<h1>Whitelabel Error Page</h1>\n"
                                                    + "\t\t<p>This application has no explicit"
                                                    + " mapping for /error, so you are seeing this"
                                                    + " as a fallback.</p>\n"
                                                    + "\t\t<div id='created'>Mon Feb 01 16:52:17"
                                                    + " CET 2021</div>\n"
                                                    + "\t\t<div>There was an unexpected error"
                                                    + " (type=Server Error, status=500).</div>\n"
                                                    + "\t\t<div></div>\n"
                                                    + "\t</body>\n"
                                                    + "</html>"
                                    )
                            )
                    )
            }
    )
    public String getMetaData() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("infomodel.json");
        assert in != null;
        InputStreamReader streamReader =
                new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        StringBuilder result = new StringBuilder();
        String line = "";

        while (true) {
            try {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            result.append(line);
        }

        return result.toString();
    }

}
