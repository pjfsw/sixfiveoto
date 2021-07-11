#importonce
    .const LOAD_ADDRESS = $0200
    .const LOAD_PAGES = LOAD_ADDRESS // $200
    .const NMI = LOAD_PAGES + 1 // $201
    .const IRQ = NMI + 3 // $204
    .const START_ADDRESS = IRQ + 3 // $207