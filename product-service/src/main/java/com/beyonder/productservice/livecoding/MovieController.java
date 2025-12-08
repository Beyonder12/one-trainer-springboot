package com.beyonder.productservice.livecoding;

import com.beyonder.productservice.exception.ProductNotFoundException;
import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    List<String> dbNames = new ArrayList<>();

    @PostMapping("/creation")
    public String createMovie(@RequestBody Map<String, String> body) {
        String inputName = body.get("name");
        dbNames.add(inputName);
        return inputName + " has been created!";
    }

    @GetMapping("/all-movie-names")
    public List<String> getMoviesNames() {

        throw new ProductNotFoundException("ProductNotFoundException");
    }

    @PutMapping("/edit/{name}")
    public String updateMovie(
            @PathVariable("name") String inputName,
            @PathParam("newName") String newName
    ) {
        for(int i = 0; i < dbNames.size(); i++){
            if(dbNames.get(i).equals(inputName)){
                dbNames.set(i, newName);
            }
        }
        return newName + " has been updated!";
    }

    @DeleteMapping("/delete/{nameToBeDeleted}")
    public String deleteMovie(@PathVariable("nameToBeDeleted") String name) {
        for(int i = 0; i < dbNames.size(); i++) {
            if(dbNames.get(i).equals(name)){
                dbNames.remove(i);
            }
        }
        return name + " has been deleted!";
    }
}
