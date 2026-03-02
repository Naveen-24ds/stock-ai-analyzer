package nd.com.demo;

import nd.com.demo.model.StockResponse;
import nd.com.demo.service.StockService;
import org.springframework.web.bind.annotation.*;

@RestController
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/analyze")
    public StockResponse analyze(@RequestParam String symbol) {
        return stockService.analyze(symbol);
    }
}