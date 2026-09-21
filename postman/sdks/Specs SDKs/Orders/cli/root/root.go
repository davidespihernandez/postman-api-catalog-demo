package root

import (
	"errors"
	orderssdk "example.com/orders/sdk"
	"example.com/orders/sdk/orderssdkconfig"
	"fmt"
	cc "github.com/ivanpirog/coloredcobra"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"os"
)

var rootCmd = &cobra.Command{
	Use:          "orders",
	Short:        "orders CLI",
	SilenceUsage: true,
}

var authConfigurators []func(client any)

func Execute() {
	cc.Init(&cc.Config{
		RootCmd:  rootCmd,
		Headings: cc.HiYellow + cc.Bold + cc.Underline,
		Commands: cc.HiGreen + cc.Bold,
		Example:  cc.Italic,
		ExecName: cc.Bold,
		Flags:    cc.Bold,
	})
	if err := rootCmd.Execute(); err != nil {
		printResponseBody(err)
		os.Exit(1)
	}
}

func printResponseBody(err error) {
	type bodyGetter interface{ GetBody() []byte }
	var e bodyGetter
	if errors.As(err, &e) {
		if body := e.GetBody(); len(body) > 0 {
			fmt.Fprintln(os.Stderr, string(body))
		}
	}
}

func AddCommand(cmd *cobra.Command) {
	rootCmd.AddCommand(cmd)
}

func RegisterAuthConfigurator(fn func(client any)) {
	authConfigurators = append(authConfigurators, fn)
}

func CreateSdkClient() *orderssdk.OrdersSDK {
	sdkConfig := orderssdkconfig.NewConfig()

	if baseUrl := viper.GetString("base_url"); baseUrl != "" {
		sdkConfig.SetBaseURL(baseUrl)
	}

	client := orderssdk.NewOrdersSDK(sdkConfig)

	// Apply credentials file values (lower priority than env vars / config file).
	for _, configure := range authConfigurators {
		configure(client)
	}

	// Re-apply viper values last so env vars and config file always win over
	// any values loaded from the credentials file above.
	return client
}

func init() {
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath("./config")
	viper.SetEnvPrefix("ORDERS")
	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		if _, ok := err.(viper.ConfigFileNotFoundError); !ok {
			fmt.Fprintf(os.Stderr, "Warning: error reading config file: %v\n", err)
		}
	}
}
