import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        AppModuleKt.initKoinHelper()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}