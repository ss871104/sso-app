package kafka

type OAuth2Message struct {
    OAuth2Id     string `json:"oAuth2Id"`
    Provider     string `json:"provider"`
    Email        string `json:"email"`
    FullName     string  `json:"fullName"`
	FamilyName   string  `json:"familyName"`
	GivenName    string  `json:"givenName"`
}